BEGIN;
CREATE TABLE IF NOT EXISTS public.reservas_stock (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 carrito_id uuid NOT NULL,
 producto_id bigint NOT NULL REFERENCES public.productos(id),
 cantidad integer NOT NULL CHECK (cantidad > 0),
 creado_en timestamptz NOT NULL DEFAULT now(),
 expira_en timestamptz NOT NULL DEFAULT (now() + interval '5 minutes'),
 UNIQUE (carrito_id, producto_id)
);
ALTER TABLE public.reservas_stock ALTER COLUMN expira_en SET DEFAULT (now() + interval '5 minutes');
ALTER TABLE public.reservas_stock ENABLE ROW LEVEL SECURITY;
CREATE INDEX IF NOT EXISTS idx_reservas_producto ON public.reservas_stock(producto_id, expira_en);
-- Los pedidos pendientes y confirmados siguen reservando hasta su atención/cancelación.
-- Requiere que el administrador descuente stock físico SOLO al atender la venta.
CREATE OR REPLACE VIEW public.v_stock_disponible WITH (security_invoker=true) AS
SELECT p.id AS producto_id, p.stock AS stock_fisico,
 COALESCE(r.cantidad,0)::bigint AS stock_reservado,
 GREATEST(p.stock - COALESCE(r.cantidad,0) - COALESCE(d.cantidad,0),0)::bigint AS stock_disponible,
 COALESCE(d.cantidad,0)::bigint AS stock_pedidos
FROM public.productos p
LEFT JOIN (SELECT producto_id, sum(cantidad) cantidad FROM public.reservas_stock
 WHERE expira_en > clock_timestamp() GROUP BY producto_id) r ON r.producto_id=p.id
LEFT JOIN (SELECT d.producto_id, sum(d.cantidad) cantidad FROM public.pedido_detalles d
 JOIN public.pedidos pe ON pe.id=d.pedido_id WHERE pe.estado IN ('Pendiente','Confirmado')
 GROUP BY d.producto_id) d ON d.producto_id=p.id;
REVOKE ALL ON public.reservas_stock, public.v_stock_disponible FROM anon, authenticated;
COMMIT;
-- Consulta de verificación (solo lectura):
SELECT p.nombre,v.* FROM public.v_stock_disponible v JOIN public.productos p ON p.id=v.producto_id ORDER BY p.nombre;

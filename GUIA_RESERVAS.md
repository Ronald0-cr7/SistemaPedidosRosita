# Minimarket Rosita — Reservas de cinco minutos

## Antes de empezar

Esta versión mantiene JSP, Servlets, Java 21, Tomcat y Supabase. Es una versión de integración para probar en local. No utiliza Spring Boot. Haz una copia de seguridad del proyecto y prueba con una base de datos de desarrollo antes de ejecutar el SQL en producción.

**Condición importante:** el sistema administrativo debe descontar `productos.stock` una sola vez cuando atiende la venta y cambiar el pedido a `Atendido` en la misma transacción. Mientras esté `Pendiente` o `Confirmado`, sus detalles se descuentan del stock disponible (pero no del físico). Si el administrador descuenta antes o utiliza otros estados, hay que adaptar ese flujo antes de publicar. Los otros procesos de venta deben respetar las reservas vigentes; no se modificó el sistema administrativo porque no fue adjuntado en esta tarea.

## 1. Supabase

Abre SQL Editor y ejecuta `reservas_stock.sql`. Crea la tabla, establece cinco minutos, habilita RLS y crea la vista de stock. El script no elimina los productos ni modifica su stock físico. Si existe una vista incompatiblemente diferente, no la elimines a ciegas: revisa el mensaje de PostgreSQL.

## 2. NetBeans

Abre esta carpeta como proyecto Maven. Configura `DB_PASSWORD` como variable de entorno de Windows con la contraseña de Supabase local/de prueba, reinicia NetBeans, ejecuta Clean and Build y Run. Comprueba que inicia Tomcat 10.1. No incluyas credenciales en GitHub.

## 3. Prueba concurrente

1. Abre el catálogo en Chrome normal y Edge o Chrome incógnito (no dos pestañas de la misma sesión).
2. Escoge un producto con stock conocido. Agrega una unidad en A; en B, el stock debe bajar en unos cinco segundos.
3. Agrega otra unidad en B; A debe mostrar la nueva disponibilidad. El carrito de A debe contener solo sus productos.
4. Elimina una unidad en A; B debe recuperarla sin recargar.
5. Cierra A sin confirmar y espera cinco minutos desde la última modificación que reservó unidades. B debe recuperar sus reservas restantes. La actualización automática no renueva el plazo.
6. Con stock disponible de una unidad, intenta agregarla en ambos navegadores a la vez: solo una reserva debe aceptarse.
7. Confirma un pedido de prueba. Las reservas temporales pasan a ser un pedido pendiente y no deben liberar las unidades hasta su cancelación o atención.
8. Prueba cancelación y atención desde el administrador. Verifica el stock físico y disponible. Si no coinciden con la regla anterior, no despliegues todavía.

Consulta de comprobación:

```sql
SELECT p.nombre,v.stock_fisico,v.stock_reservado,v.stock_pedidos,v.stock_disponible
FROM public.v_stock_disponible v JOIN public.productos p ON p.id=v.producto_id;
```

## 4. Render

Después de las pruebas, sube los archivos modificados a GitHub, comprueba DB_PASSWORD y PORT=8080 en Render y espera el despliegue automático. Prueba nuevamente con perfiles de navegador separados. No hace falta modificar el Dockerfile.

## Notas

Cada modificación que agrega o aumenta/disminuye una cantidad positiva renueva las reservas vigentes del carrito a cinco minutos. Eliminar o vaciar libera inmediatamente. Las filas expiradas no cuentan en la disponibilidad; se pueden limpiar periódicamente con una tarea de mantenimiento. El contador se actualiza cada segundo y el catálogo consulta disponibilidad cada cinco segundos. Los registros de carrito no se comparten entre sesiones aunque se comparta la base de datos.

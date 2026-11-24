/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author ronaldo
 */
public class Pedido {
    private String id;
    private String numero;
    private String nombreContacto;
     private String telefonoContacto;
    private String tipoEntrega; //local o delivery
    private String direccionEntrega;
     private String metodoPago;
    private String estado;//Estado del pedido (atendido, cancelado o pendiente)
    private String observaciones;
     private String creadoEn; //Fecha de registro del pedido
    private double costoDelibery;
   private double subtotal;
   private double totalEstimado;
    private String cuentaClienteId;

  
    public Pedido() {
    }

    public Pedido(String id, String numero, String nombreContacto, String telefonoContacto, String tipoEntrega, String direccionEntrega, String metodoPago, String estado, String observaciones, String creadoEn, double costoDelibery, double subtotal, double totalEstimado, String cuentaClienteId) {
        this.id = id;
        this.numero = numero;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.tipoEntrega = tipoEntrega;
        this.direccionEntrega = direccionEntrega;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.observaciones = observaciones;
        this.creadoEn = creadoEn;
        this.costoDelibery = costoDelibery;
        this.subtotal = subtotal;
        this.totalEstimado = totalEstimado;
        this.cuentaClienteId = cuentaClienteId;
    }

    public String getCuentaClienteId() {
        return cuentaClienteId;
    }

    public void setCuentaClienteId(String cuentaClienteId) {
        this.cuentaClienteId = cuentaClienteId;
    }





    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(String creadoEn) {
        this.creadoEn = creadoEn;
    }

    public double getCostoDelibery() {
        return costoDelibery;
    }

    public void setCostoDelibery(double costoDelibey) {
        this.costoDelibery = costoDelibey;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalEstimado() {
        return totalEstimado;
    }

    public void setTotalEstimado(double totalEstimado) {
        this.totalEstimado = totalEstimado;
    }


 
   
   
   
    
    
}

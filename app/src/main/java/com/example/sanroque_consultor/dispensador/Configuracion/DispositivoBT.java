package com.example.sanroque_consultor.dispensador.Configuracion;

import android.hardware.usb.UsbDevice;

import java.io.Serializable;

public class DispositivoBT implements Serializable {

    private String nombreDispositivo;
    private String macDispositivo;
    private String tipoDispositivo;
    private transient UsbDevice usb;


    public DispositivoBT() {
    }

    public UsbDevice getUsb() {
        return usb;
    }

    public void setUsb(UsbDevice usb) {
        this.usb = usb;
    }

    public String getNombreDispositivo() {
        return nombreDispositivo;
    }

    public void setNombreDispositivo(String nombreDispositivo) {
        this.nombreDispositivo = nombreDispositivo;
    }

    public String getMacDispositivo() {
        return macDispositivo;
    }

    public void setMacDispositivo(String macDispositivo) {
        this.macDispositivo = macDispositivo;
    }

    public String getTipoDispositivo() {
        return tipoDispositivo;
    }

    public void setTipoDispositivo(String tipoDispositivo) {
        this.tipoDispositivo = tipoDispositivo;
    }
}

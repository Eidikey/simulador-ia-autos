package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PartidaGuardada {
    private final String timestamp;
    private final String resultado;
    private final double distanciaJugador;
    private final double distanciaIA;
    private final boolean metaJugador;
    private final boolean metaIA;
    private final int framesDuracion;

    public PartidaGuardada(String resultado, double distJugador, double distIA,
                           boolean metaJugador, boolean metaIA, int framesDuracion) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.resultado = resultado;
        this.distanciaJugador = Math.round(distJugador * 100.0) / 100.0;
        this.distanciaIA = Math.round(distIA * 100.0) / 100.0;
        this.metaJugador = metaJugador;
        this.metaIA = metaIA;
        this.framesDuracion = framesDuracion;
    }

    public String getTimestamp() { return timestamp; }
    public String getResultado() { return resultado; }
    public double getDistanciaJugador() { return distanciaJugador; }
    public double getDistanciaIA() { return distanciaIA; }
    public boolean isMetaJugador() { return metaJugador; }
    public boolean isMetaIA() { return metaIA; }
    public int getFramesDuracion() { return framesDuracion; }
}

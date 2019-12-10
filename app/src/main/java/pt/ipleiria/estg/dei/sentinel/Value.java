package pt.ipleiria.estg.dei.sentinel;

import java.io.Serializable;
import java.util.Date;

public class Value implements Serializable {

    private Date date;
    private float temperatura;
    private float humidadade;

    public Value(Date date, float temperatura, float humidadade) {
        this.date = date;
        this.temperatura = temperatura;
        this.humidadade = humidadade;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(float temperatura) {
        this.temperatura = temperatura;
    }

    public float getHumidadade() {
        return humidadade;
    }

    public void setHumidadade(float humidadade) {
        this.humidadade = humidadade;
    }
}

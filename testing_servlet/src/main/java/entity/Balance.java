package entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by USER on 20.07.2017.
 */

@Entity(name = "balance")
public class Balance implements Serializable{
    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private Float total;

    public Balance() {
    }

    public Balance(Float total) {
        this.total = total;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "id=" + id +
                ", total=" + total +
                '}';
    }
}

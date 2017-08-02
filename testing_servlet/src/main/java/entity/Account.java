package entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by USER on 20.07.2017.
 */
@NamedQueries({
        @NamedQuery(name = "Account.isExists", query = "SELECT a FROM account a WHERE a.phone = :phone")
})
@Entity(name = "account")
public class Account implements Serializable{
    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String phone;
    @Column
    private String password;

    @OneToOne(targetEntity = Balance.class)
    private Balance balance;

    public Account() {
    }

    public Account(String phone, String password, Balance balance) {
        this.phone = phone;
        this.password = password;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                '}';
    }
}

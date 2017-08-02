package service;

import entity.Account;
import entity.Balance;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 * Created by USER on 20.07.2017.
 */
public class Service {

    public static EntityManager em = Persistence.createEntityManagerFactory("NCEDU").createEntityManager();

    public static Account addAccount(Account account){
        em.getTransaction().begin();
        Account result = em.merge(account);
        em.getTransaction().commit();
        return result;
    }

    public static Balance addBalance(Balance balance){
        em.getTransaction().begin();
        Balance result = em.merge(balance);
        em.getTransaction().commit();
        return result;
    }

    public static Balance setTotalBalance(Balance balance, Float total){
        em.getTransaction().begin();
        Balance result = em.find(Balance.class, balance.getId());
        result.setTotal(total);
        em.getTransaction().commit();
        return result;
    }

    public static Account getAccountByPhone(String phone){
        TypedQuery<Account> query = em.createNamedQuery("Account.isExists", Account.class)
                .setParameter("phone", phone);
        return query.getResultList().size() != 0 ? query.getResultList().get(0) : null;
    }

    public static boolean isFree(String phone){
        TypedQuery<Account> query = em.createNamedQuery("Account.isExists", Account.class)
                .setParameter("phone", phone);
        return query.getResultList().size() == 0;
    }
}

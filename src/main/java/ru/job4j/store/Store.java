package ru.job4j.store;

import ru.job4j.model.Account;
import ru.job4j.model.Ticket;

import java.util.Collection;

public interface Store {
    void save(Account account);
    void save(Ticket ticket);
    Collection<Account> findAllAccounts();
    Collection<Ticket> findAllTickets();
    Account findAccountById(int id);
    Ticket findTicketById(int id);
    Account findAccountByEmail(String email);
    Account findAccountByPhone(String phone);
}

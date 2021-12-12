package ru.job4j.store;

import ru.job4j.model.Account;
import ru.job4j.model.Ticket;

import java.sql.SQLException;
import java.util.Collection;

public interface Store {
    Account save(Account account);
    Ticket save(Ticket ticket) throws SQLException;
    Collection<Account> findAllAccounts();
    Collection<Ticket> findAllTickets();
    Account findAccountById(int id);
    Ticket findTicketById(int id);
    Account findAccountByEmail(String email);
    Account findAccountByPhone(String phone);
}

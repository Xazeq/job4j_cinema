package ru.job4j.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.model.Account;
import ru.job4j.model.Ticket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class DbStore implements Store {
    private static final Logger LOG = LoggerFactory.getLogger(DbStore.class.getName());
    private final BasicDataSource pool = new BasicDataSource();

    private DbStore() {
        Properties cfg = new Properties();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        DbStore.class.getClassLoader()
                                .getResourceAsStream("db.properties")
                )
        )) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new DbStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    @Override
    public void save(Account account) {
        if (account.getId() == 0) {
            create(account);
        } else {
            update(account);
        }
    }

    private Account create(Account account) {
        try (Connection cn  = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO account(username, email, phone) VALUES (?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS
             )) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPhone());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    account.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception when create account", e);
        }
        return account;
    }

    private void update(Account account) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE account SET username = ?, email = ?, phone = ? WHERE id = ?")
        ) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPhone());
            ps.setInt(4, account.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception when update account", e);
        }
    }

    @Override
    public void save(Ticket ticket) {
        if (ticket.getId() == 0) {
            create(ticket);
        } else {
            update(ticket);
        }
    }

    private Ticket create(Ticket ticket) {
        try (Connection cn  = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO ticket(session_id, row, cell, account_id) VALUES (?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS
             )) {
            ps.setInt(1, ticket.getSessionId());
            ps.setInt(2, ticket.getRow());
            ps.setInt(3, ticket.getCell());
            ps.setInt(4, ticket.getAccount().getId());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    ticket.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception when create ticket", e);
        }
        return ticket;
    }

    private void update(Ticket ticket) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE ticket SET session_id = ?, row = ?, cell = ?, account_id = ? WHERE id = ?")
        ) {
            ps.setInt(1, ticket.getSessionId());
            ps.setInt(2, ticket.getRow());
            ps.setInt(3, ticket.getCell());
            ps.setInt(4, ticket.getAccount().getId());
            ps.setInt(5, ticket.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error("Exception when update ticket", e);
        }
    }

    @Override
    public Collection<Account> findAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement("SELECT  * FROM account")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new Account(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("phone")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in findAllAccounts method", e);
        }
        return accounts;
    }

    @Override
    public Collection<Ticket> findAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement(
                "SELECT t.id as t_id, t.session_id as t_session_id, t.row as t_row, t.cell as t_cell,"
                        + " a.id as a_id, a.username as a_username, a.email as a_email, a.phone as a_phone "
                + "FROM ticket t "
                + "JOIN account a ON a.id = t.account_id"
        )) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(new Ticket(rs.getInt("t_id"),
                            rs.getInt("t_session_id"),
                            rs.getInt("t_row"),
                            rs.getInt("t_cell"),
                            new Account(rs.getInt("a_id"),
                                    rs.getString("a_username"),
                                    rs.getString("a_email"),
                                    rs.getString("a_phone"))));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in findAllTickets method", e);
        }
        return tickets;
    }

    @Override
    public Account findAccountById(int id) {
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement("SELECT * FROM account WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("phone"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in findAccountById method", e);
        }
        return null;
    }

    @Override
    public Ticket findTicketById(int id) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT t.id as t_id, t.session_id as t_session_id, t.row as t_row, t.cell as t_cell,"
                             + " a.id as a_id, a.username as a_username, a.email as a_email, a.phone as a_phone "
                             + "FROM ticket t "
                             + "JOIN account a ON a.id = t.account_id "
                             + "WHERE t.id = ?"
             )) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ticket(rs.getInt("t_id"),
                            rs.getInt("t_session_id"),
                            rs.getInt("t_row"),
                            rs.getInt("t_cell"),
                            new Account(rs.getInt("a_id"),
                                    rs.getString("a_username"),
                                    rs.getString("a_email"),
                                    rs.getString("a_phone")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in findTicketById method", e);
        }
        return null;
    }

    @Override
    public Account findAccountByEmail(String email) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM account WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("phone"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in findAccountByEmail method", e);
        }
        return null;
    }

    @Override
    public Account findAccountByPhone(String phone) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM account WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("phone"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in findAccountByPhone method", e);
        }
        return null;
    }
}

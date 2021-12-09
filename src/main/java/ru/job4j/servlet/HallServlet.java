package ru.job4j.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.model.Account;
import ru.job4j.model.Ticket;
import ru.job4j.store.DbStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HallServlet extends HttpServlet {
    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; character=utf-8");
        OutputStream out = resp.getOutputStream();
        String json = GSON.toJson(DbStore.instOf().findAllTickets());
        out.write(json.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (DbStore.instOf().findAccountByEmail(req.getParameter("email")) == null) {
            DbStore.instOf().save(new Account(
                    0,
                    req.getParameter("username"),
                    req.getParameter("email"),
                    req.getParameter("phone")
            ));
        }
        Account account = DbStore.instOf().findAccountByEmail(req.getParameter("email"));
        DbStore.instOf().save(new Ticket(
                0, 1, Integer.parseInt(req.getParameter("row")),
                Integer.parseInt(req.getParameter("cell")),
                account
        ));
    }
}

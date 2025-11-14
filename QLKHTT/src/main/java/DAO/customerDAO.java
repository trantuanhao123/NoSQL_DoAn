package DAO;

import MODELS.Customer;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private final CqlSession session;

    public CustomerDAO(CqlSession session) {
        this.session = session;
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        ResultSet rs = session.execute("SELECT * FROM customers");
        for (Row row : rs) {
            customers.add(mapRow(row));
        }
        return customers;
    }

    public Customer findById(String id) {
        PreparedStatement ps = session.prepare("SELECT * FROM customers WHERE customer_id = ?");
        BoundStatement bs = ps.bind(id);
        Row row = session.execute(bs).one();
        return row != null ? mapRow(row) : null;
    }

    public void save(Customer c) {
        PreparedStatement ps = session.prepare("""
            INSERT INTO customers (
                customer_id, full_name, email, phone, dob, gender, address, created_at, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """);
        BoundStatement bs = ps.bind(
                c.getCustomerId(),
                c.getFullName(),
                c.getEmail(),
                c.getPhone(),
                c.getDob(),
                c.getGender(),
                c.getAddress(),
                c.getCreatedAt(),
                c.getStatus()
        );
        session.execute(bs);
    }

    public void delete(String id) {
        PreparedStatement ps = session.prepare("DELETE FROM customers WHERE customer_id = ?");
        session.execute(ps.bind(id));
    }

    private Customer mapRow(Row row) {
        Customer c = new Customer();
        c.setCustomerId(row.getString("customer_id"));
        c.setFullName(row.getString("full_name"));
        c.setEmail(row.getString("email"));
        c.setPhone(row.getString("phone"));
        c.setDob(row.getLocalDate("dob"));
        c.setGender(row.getString("gender"));
        c.setAddress(row.getString("address"));
        c.setCreatedAt(row.getInstant("created_at"));
        c.setStatus(row.getString("status"));
        return c;
    }
}

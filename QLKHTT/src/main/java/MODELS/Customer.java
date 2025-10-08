package MODELS;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@CqlName("customers")
public class Customer {
    @PartitionKey
    @CqlName("customer_id")
    private UUID customerId;

    @CqlName("full_name")
    private String fullName;

    @CqlName("email")
    private String email;

    @CqlName("phone")
    private String phone;

    @CqlName("dob")
    private LocalDate dob;

    @CqlName("gender")
    private String gender;

    @CqlName("address")
    private String address;

    @CqlName("created_at")
    private Instant createdAt;  // <-- Chỉ dùng Instant

    @CqlName("status")
    private String status;

    // Constructors
    public Customer() {}

    public Customer(UUID customerId, String fullName, String email, String phone, LocalDate dob, String gender, String address, Instant createdAt, String status) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.createdAt = createdAt;  // <-- Instant
        this.status = status;
    }

    // Getters and Setters
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Instant getCreatedAt() { return createdAt; }  // <-- Instant
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

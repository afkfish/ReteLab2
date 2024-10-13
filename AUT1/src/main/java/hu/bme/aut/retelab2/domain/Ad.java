package hu.bme.aut.retelab2.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
public class Ad {

    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return id;
    }

    public String secret;

    public String title;

    public String description;

    @ElementCollection
    public List<String> tags;

    public int price;

    public LocalDateTime expirationDate;

    public Date creationDate;
}

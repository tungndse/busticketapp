package springmockproject.busticketapp.entity;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "Location")
public class Location {

    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "Name")
    private String name;


}

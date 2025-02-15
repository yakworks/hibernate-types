package com.vladmihalcea.hibernate.type.json.generic;

import com.vladmihalcea.hibernate.type.json.JsonType;
import com.vladmihalcea.hibernate.util.AbstractPostgreSQLIntegrationTest;
import org.hibernate.annotations.TypeDef;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Vlad Mihalcea
 */
public class GenericOffsetDateTimeJsonTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Event.class,
        };
    }

    @Test
    public void test() {
        OffsetDateTime dateTime = OffsetDateTime.of(2015, 10, 1, 9, 0, 0, 0, ZoneOffset.ofHours(2));

        doInJPA(entityManager -> {
            Location location = new Location();
            location.setCountry("Romania");
            location.setCity("Cluj-Napoca");
            location.setRentedAt(dateTime);

            Event event = new Event();
            event.setId(1L);
            event.setLocation(location);
            entityManager.persist(event);
        });

        doInJPA(entityManager -> {
            Event event = entityManager.find(Event.class, 1L);
            assertEquals(dateTime, event.getLocation().getRentedAt());
        });
    }

    @Test
    public void testWithDouble() {
        OffsetDateTime dateTime = OffsetDateTime.of(2022, 7, 14, 11, 37, 0, 0, ZoneOffset.ofHours(0));

        //2022-07-14T11:37Z
        String dateAsDouble = "1657798620.121624";
        String json = String.format("{\n" +
                "\"country\":\"Romania\",\n" +
                "\"city\":\"Cluj-Napoca\",\n" +
                "\"rentedAt\": %s\n" +
                "}", dateAsDouble);

        doInJDBC(connection -> {
            String sql = "INSERT INTO EVENT (ID, LOCATION) VALUES (1, ?::JSON)";
            try (
                    PreparedStatement statement = connection.prepareStatement(sql);
            ) {
                statement.setObject(1, json);
                statement.executeUpdate();
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        });

        doInJPA(entityManager -> {
            Event event = entityManager.find(Event.class, 1L);
            assertEquals(dateTime, event.getLocation().getRentedAt());
        });
    }

    @Entity(name = "Event")
    @Table(name = "event")
    @TypeDef(defaultForType = Location.class, typeClass = JsonType.class)
    public static class Event {

        @Id
        private Long id;

        @Column(columnDefinition = "jsonb")
        private Location location;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    public static class Location implements Serializable {

        private String country;

        private String city;

        private BigDecimal reference;

        private OffsetDateTime rentedAt;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public BigDecimal getReference() {
            return reference;
        }

        public void setReference(BigDecimal reference) {
            this.reference = reference;
        }

        public OffsetDateTime getRentedAt() {
            return rentedAt;
        }

        public void setRentedAt(OffsetDateTime rentedAt) {
            this.rentedAt = rentedAt;
        }
    }
}

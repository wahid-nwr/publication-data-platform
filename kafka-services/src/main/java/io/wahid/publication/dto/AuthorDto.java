package io.wahid.publication.dto;

import com.opencsv.bean.CsvBindByPosition;

public class AuthorDto {
    @CsvBindByPosition(position = 0, required = true)
    private String email;
    @CsvBindByPosition(position = 1, required = true)
    private String firstName;
    @CsvBindByPosition(position = 2, required = true)
    private String lastName;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + email + ")";
    }

    // required public no args constructor by opencsv
    public AuthorDto() {}

    public AuthorDto(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

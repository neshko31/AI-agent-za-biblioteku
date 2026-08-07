package ftn.iis.dto;

import jakarta.validation.constraints.Email;

public class UpdateProfilDto {
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String phone;
    private String newPassword;

    public UpdateProfilDto(String firstName, String lastName, String email, String phone, String newPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.newPassword = newPassword;
    }

    public UpdateProfilDto() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

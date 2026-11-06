package br.com.doceterapia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioCriacaoDto {

    @NotBlank(message = "Informe o nome.")
    @Size(min = 3, max = 50, message = "O nome deve ter de 3 a 50 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "Informe a senha.")
    @Size(min = 6, max = 20, message = "A senha deve ter de 6 a 20 caracteres.")
    private String senha;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

package amodels;

import java.sql.Timestamp;

public class Product {

    private Integer id;
    private String name;
    private Double price; 
    private String description;
    private String foto; 
    private KategoriProduk category; 
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    
    public Product() {
    }

    public Product(String name, Double price, String description, String foto, KategoriProduk category) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.foto = foto;
        this.category = category;
        this.isActive = true;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public KategoriProduk getCategory() {
        return category;
    }

    public void setCategory(KategoriProduk category) {
        this.category = category;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    
//    @Override
//    public String toString() {
//        return "Produk{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", price=" + price +
//                ", category=" + category +
//                '}';
//    }
}

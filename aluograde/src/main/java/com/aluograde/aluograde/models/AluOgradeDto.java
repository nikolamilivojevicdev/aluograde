package com.aluograde.aluograde.models;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class AluOgradeDto {
	@NotEmpty(message="the name is required")
	private String name;
	@NotEmpty(message="the category is required")
	private String category;
	@Min(0)
	private double price;
	@Min(0)
	private double amount;
	@Size(min=10, message="the description should be at least 10 characters")
	@Size(max=2000, message="the description cannot exceed 2000 characters")
	private String description;
	private MultipartFile imageFile;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public MultipartFile getImageFile() {
		return imageFile;
	}
	public void setImageFile(MultipartFile imageFile) {
		this.imageFile = imageFile;
	}
}

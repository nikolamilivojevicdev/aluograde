package com.aluograde.aluograde.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aluograde.aluograde.models.AluOgrade;
import com.aluograde.aluograde.models.AluOgradeDto;
import com.aluograde.aluograde.services.OgradeRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
@RequestMapping("/ograde")
public class OgradeController {
	  @Autowired
	    private OgradeRepository repo;

	  @GetMapping({"", "/"})
	  public String showOgradeList (@RequestParam(name = "keyword", required = false) String keyword,
	      @RequestParam(name = "sort", required = false, defaultValue = "desc") String sort, Model model) {

	        List<AluOgrade> ograde;

	        if (keyword != null && !keyword.isEmpty()) {
	            ograde = repo.findByNameContaining(keyword, Sort.by(Sort.Direction.fromString(sort), "id"));
	        } else {
	            // Sortiraj proizvode po ID-u (uzlazno ili silazno)
	            ograde = repo.findAll(Sort.by(Sort.Direction.fromString(sort), "id"));
	        }
	        
	        model.addAttribute("ograde", ograde);
	        model.addAttribute("keyword", keyword); // Za vraćanje ključne reči u formu
	     // Proveri da li je korisnik prijavljen
	        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
	            !SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
	            
	            // Ako je prijavljen, proveri da li je admin
	            if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
	                    .anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
	                return "ograde/index";  // Admin vidi ovu stranu sa CRUD opcijama
	            } else {
	                return "ograde/client";  // Prijavljeni korisnik (klijent) vidi ovu stranu bez CRUD opcija
	            }
	        } else {
	            return "ograde/client";  // Nelogovan korisnik vidi klijent stranu
	        }
		}
	  
	 
	  @GetMapping("/create")
	    public String showCreatePage(Model model) {
	        AluOgradeDto ogradeDto = new AluOgradeDto();
	        model.addAttribute("ogradeDto", ogradeDto);
	        return "ograde/CreateOgrade";
	    }
	    @PostMapping("/create")
	    public String createOgrade(@Valid @ModelAttribute AluOgradeDto ogradeDto, BindingResult result) {
	        if (ogradeDto.getImageFile().isEmpty()) {
	            result.addError(new FieldError("ogradeDto", "imageFile", "The image file is required"));
	        }
	        if (result.hasErrors()) {
	            return "ograde/CreateOgrade";
	        }

	        // Save image file
	        MultipartFile image = ogradeDto.getImageFile();
	        Date createdAt = new Date();
	        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

	        try {
	            String uploadDir = "public/images/";
	            Path uploadPath = Paths.get(uploadDir);

	            if (!Files.exists(uploadPath)) {
	                Files.createDirectories(uploadPath);
	            }

	            try (InputStream inputStream = image.getInputStream()) {
	                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
	            }
	        } catch (Exception ex) {
	            System.out.println("Exception:" + ex.getMessage());
	        }

	        AluOgrade ograde = new AluOgrade();
	        ograde.setName(ogradeDto.getName());
	        ograde.setCategory(ogradeDto.getCategory());
	        ograde.setPrice(ogradeDto.getPrice());
	        ograde.setAmount(ogradeDto.getAmount());
	        ograde.setDescription(ogradeDto.getDescription());
	        ograde.setCreatedAt(createdAt);
	        ograde.setImageFileName(storageFileName);
	        repo.save(ograde);

	        return "redirect:/ograde";
	    }
	    @GetMapping("/edit")
	    public String showEditPage(Model model, @RequestParam int id) {
	        try {
	            AluOgrade ograde = repo.findById(id).get();
	            model.addAttribute("ograde", ograde);

	            AluOgradeDto ogradeDto = new AluOgradeDto();
	            ogradeDto.setName(ograde.getName());
	            ogradeDto.setCategory(ograde.getCategory());
	            ogradeDto.setPrice(ograde.getPrice());
	            ogradeDto.setAmount(ograde.getAmount());
	            ogradeDto.setDescription(ograde.getDescription());
	            model.addAttribute("ogradeDto", ogradeDto);
	        } catch (Exception ex) {
	            System.out.println("Exception:" + ex.getMessage());
	            return "redirect:/ograde";
	        }

	        return "ograde/EditOgrade";
	    }

	    @PostMapping("/edit")
	    public String updateOgrade(Model model, @RequestParam int id, @Valid @ModelAttribute AluOgradeDto ogradeDto, BindingResult result) {
	        try {
	            AluOgrade ograde = repo.findById(id).get();
	            model.addAttribute("ograde", ograde);

	            if (result.hasErrors()) {
	                return "ograde/EditOgrade";
	            }

	            if (!ogradeDto.getImageFile().isEmpty()) {
	                // Delete old image
	                String uploadDir = "public/images/";
	                Path oldImagePath = Paths.get(uploadDir + ograde.getImageFileName());
	                try {
	                    Files.delete(oldImagePath);
	                } catch (Exception ex) {
	                    System.out.println("Exception: " + ex.getMessage());
	                }

	                // Save new image file
	                MultipartFile image = ogradeDto.getImageFile();
	                Date createdAt = new Date();
	                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
	                try (InputStream inputStream = image.getInputStream()) {
	                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
	                }
	                ograde.setImageFileName(storageFileName);
	            }

	            ograde.setName(ogradeDto.getName());
	            ograde.setCategory(ogradeDto.getCategory());
	            ograde.setPrice(ogradeDto.getPrice());
	            ograde.setAmount(ogradeDto.getAmount());
	            ograde.setDescription(ogradeDto.getDescription());

	            repo.save(ograde);
	        } catch (Exception ex) {
	            System.out.println("exception:" + ex.getMessage());
	        }

	        return "redirect:/ograde";
	    }
	    @GetMapping("/delete")
	    public String deleteOgrade(@RequestParam int id) {
	        try {
	            AluOgrade ograde = repo.findById(id).get();

	            // Delete product image
	            Path imagePath = Paths.get("public/images/" + ograde.getImageFileName());
	            try {
	                Files.delete(imagePath);
	            } catch (Exception ex) {
	                System.out.println("Exception: " + ex.getMessage());
	            }
	            

	            repo.delete(ograde);
	        } catch (Exception ex) {
	            System.out.println("Exception: " + ex.getMessage());
	        }

	        return "redirect:/ograde";
	    }
	    @GetMapping("/login")
	    public String loginPage() {
	        return "/ograde/login"; // Ova putanja treba da odgovara lokaciji gde je `login.html`
	    }
	    @GetMapping("/addProduct")
	    public String addProductToList(@RequestParam(name = "id", required = false, defaultValue = "0") int id, HttpSession session, Model model) {
	        if (id == 0) {
	            return "redirect:/ograde"; // Ako ID nije prisutan, vraćaj na listu proizvoda
	        }
	        
	        List<AluOgrade> addedProducts = (List<AluOgrade>) session.getAttribute("addedProducts");
	        if (addedProducts == null) {
	            addedProducts = new ArrayList<>();
	            session.setAttribute("addedProducts", addedProducts);
	        }

	        AluOgrade product = repo.findById(id).orElse(null);
	        if (product != null) {
	            // Postavi početni amount na 1
	            if (product.getAmount() == 0) {
	                product.setAmount(1);
	            }
	            addedProducts.add(product);
	        }

	        double totalPrice = 0;
	        for (AluOgrade p : addedProducts) {
	            totalPrice += p.getPrice() * p.getAmount();
	        }

	        model.addAttribute("addedProducts", addedProducts);
	        model.addAttribute("totalPrice", totalPrice);  // Izračunaj ukupnu cenu nakon dodavanja proizvoda

	        return "ograde/addProduct"; // Vraća HTML stranicu sa listom dodanih proizvoda
	    }




	    @GetMapping("/showAddedProducts")
	    public String showAddedProducts(Model model, HttpSession session) {
	        List<AluOgrade> addedProducts = (List<AluOgrade>) session.getAttribute("addedProducts");
	        
	        if (addedProducts == null) {
	            addedProducts = new ArrayList<>();
	        }

	        double totalPrice = 0;
	        
	        for (AluOgrade product : addedProducts) {
	            // Inicijalizuj amount na 1 ako je nula
	            if (product.getAmount() == 0) {
	                product.setAmount(1);
	            }
	            
	            // Izračunaj ukupnu cenu za svaki proizvod
	            totalPrice += product.getPrice() * product.getAmount();
	        }

	        model.addAttribute("addedProducts", addedProducts);
	        model.addAttribute("totalPrice", totalPrice);  // Dodaj ukupnu cenu u model

	        return "ograde/addProduct";
	    }
	    

	    @GetMapping("/removeFromList")
	    public String removeProductFromList(@RequestParam(name = "id") int id, HttpSession session) {
	        List<AluOgrade> addedProducts = (List<AluOgrade>) session.getAttribute("addedProducts");
	        
	        if (addedProducts != null) {
	            // Pronađi proizvod u listi po ID-u i ukloni ga
	            addedProducts.removeIf(product -> product.getId() == id);
	        }
	        
	        // Redirektuj na stranicu gde se prikazuju dodani proizvodi
	        return "redirect:/ograde/showAddedProducts";
	    }
	    @GetMapping("/increaseQuantity")
	    public String increaseQuantity(@RequestParam int id, HttpSession session, Model model) {
	        List<AluOgrade> addedProducts = (List<AluOgrade>) session.getAttribute("addedProducts");
	        if (addedProducts != null) {
	            for (AluOgrade product : addedProducts) {
	                if (product.getId() == id) {
	                    product.setAmount(product.getAmount() + 1);
	                }
	            }
	        }

	        // Ažuriramo listu proizvoda u sesiji
	        session.setAttribute("addedProducts", addedProducts);

	        // Računamo ukupnu cenu
	        double totalPrice = addedProducts.stream()
	                .mapToDouble(product -> product.getPrice() * product.getAmount())
	                .sum();
	        
	        model.addAttribute("addedProducts", addedProducts);
	        model.addAttribute("totalPrice", totalPrice);

	        // Ostanak na istoj stranici
	        return "ograde/addProduct";
	    }

	    @GetMapping("/decreaseQuantity")
	    public String decreaseQuantity(@RequestParam int id, HttpSession session, Model model) {
	        List<AluOgrade> addedProducts = (List<AluOgrade>) session.getAttribute("addedProducts");
	        if (addedProducts != null) {
	            for (AluOgrade product : addedProducts) {
	                if (product.getId() == id && product.getAmount() > 1) {
	                    product.setAmount(product.getAmount() - 1);
	                }
	            }
	        }

	        // Ažuriramo listu proizvoda u sesiji
	        session.setAttribute("addedProducts", addedProducts);

	        // Računamo ukupnu cenu
	        double totalPrice = addedProducts.stream()
	                .mapToDouble(product -> product.getPrice() * product.getAmount())
	                .sum();
	        
	        model.addAttribute("addedProducts", addedProducts);
	        model.addAttribute("totalPrice", totalPrice);

	        // Ostanak na istoj stranici
	        return "ograde/addProduct";
	    }
	    @GetMapping("/payment")
	    public String showPaymentPage(Model model) {
	        return "/ograde/payment"; // Ovo je ime HTML fajla za stranicu plačanja
	    }
	    @PostMapping("/processPayment")
	    public String processPayment(@RequestParam String paymentMethod,
	                                 @RequestParam(required = false) String firstName,
	                                 @RequestParam(required = false) String lastName,
	                                 @RequestParam(required = false) String address,
	                                 @RequestParam(required = false) String idNumber,
	                                 @RequestParam(required = false) String cardNumber,
	                                 @RequestParam(required = false) String jmbg,
	                                 HttpSession session) {
	        // Logika za obradu plačanja
	        if ("card".equals(paymentMethod)) {
	            // Obrada plačanja karticom
	        } else if ("cash".equals(paymentMethod)) {
	            //session.removeAttribute("addedProducts");  // Očisti listu proizvoda
	            return "redirect:/ograde/confirmation";
	        }

	        // Očisti listu proizvoda ili obavi druge potrebne akcije
	        //session.removeAttribute("addedProducts");
	        
	        return "redirect:/ograde/confirmation"; // Ova stranica prikazuje potvrdu o plačanju
	    }

	    @GetMapping("/confirmation")
	    public String showConfirmationPage(Model model, HttpSession session) {
	        List<AluOgrade> addedProducts = (List<AluOgrade>) session.getAttribute("addedProducts");

	        if (addedProducts == null) {
	            addedProducts = new ArrayList<>();
	        }

	        // Provera veličine liste i ispis u konzolu
	        //System.out.println("Veličina liste addedProducts: " + addedProducts.size());

	        double totalPrice = 0;

	        for (AluOgrade product : addedProducts) {
	            if (product.getAmount() == 0) {
	                product.setAmount(1);
	            }
	            totalPrice += product.getPrice() * product.getAmount();
	        }

	        model.addAttribute("addedProducts", addedProducts);
	        model.addAttribute("totalPrice", totalPrice);
	        session.removeAttribute("addedProducts");
	        return "/ograde/confirmation"; // Ovo je ime HTML fajla
	    }
	    @GetMapping("/product/{id}")
	    public String getProductDetails(@PathVariable Integer id, Model model) {
	        // Preuzmi podatke o proizvodu na osnovu ID-a
	    	AluOgrade ograde = repo.findById(id).get();
	        model.addAttribute("ograde", ograde);
	        return "/ograde/product-details"; // Stranica sa detaljima o proizvodu*/
	    
	    }

}


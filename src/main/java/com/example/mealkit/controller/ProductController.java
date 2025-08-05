package com.example.mealkit.controller;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Product;
import com.example.mealkit.dto.ProductRatingDto;
import com.example.mealkit.dto.ProductRequestDto;
import com.example.mealkit.dto.ProductResponseDto;
import com.example.mealkit.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @Value("${upload.path}")
    private String uploadPath;

    // 상품 등록
    @PostMapping
    public ResponseEntity<?> createProduct(@ModelAttribute ProductRequestDto dto, HttpSession session) throws IOException {
        Member member = (Member) session.getAttribute("loginMember");
               
        if (member == null || !member.isAdmin()) {
            return ResponseEntity.status(403).body("관리자만 등록 가능합니다.");
        }

        Product product = new Product();
        applyDtoToProduct(dto, product);
        product.setMember(member);

        return ResponseEntity.status(201).body(productRepository.save(product));
    }

    // 전체 상품 목록
    @GetMapping
    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAllByOrderByIdDesc();
        return toResponseDtoList(products);
    }
    //home에 있는 최근 본 상품
    @GetMapping("/recent")
    public List<ProductResponseDto> getRecentProducts(HttpSession session){
    	Deque<Long> recentIds = (Deque<Long>) session.getAttribute("recentProducts");
    	if (recentIds == null || recentIds.isEmpty()) return new ArrayList<>();
    	
    	List<Product> recentProducts = new ArrayList<>();
    	for (Long id : recentIds) {
    		productRepository.findById(id).ifPresent(recentProducts::add);
    	}
    	return toResponseDtoList(recentProducts);
    }
    
    // 카테고리 조회
    @GetMapping("/category/{category}")
    public List<ProductResponseDto> getByCategory(@PathVariable String category) {
        List<Product> products = productRepository.findByCategory(category);
        return toResponseDtoList(products);
    }

    // 세부카테고리 조회
    @GetMapping("/subcategory/{category2}")
    public List<ProductResponseDto> getBySubCategory(@PathVariable String category2) {
        List<Product> products = productRepository.findByCategory2(category2);
        return toResponseDtoList(products);
    }     

    // 상품 상세
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id, HttpSession session) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Deque<Long> recentIds = (Deque<Long>) session.getAttribute("recentProducts");
        
        if (recentIds == null) {
        	recentIds = new LinkedList<>();
        }
        recentIds.remove(id);
        recentIds.addFirst(id);
        
        while (recentIds.size() > 3) {
        	recentIds.removeLast();
        }
        session.setAttribute("recentProducts", recentIds);
        
        Product p = optional.get();
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setImagePath(p.getImagePath());
        dto.setImagePath2(p.getImagePath2());
        dto.setPrdDetail(p.getPrdDetail());
        dto.setPrdInfo(p.getPrdInfo());
        dto.setCategory(p.getCategory());
        dto.setCategory2(p.getCategory2());

        return ResponseEntity.ok(dto);
    }
    // 상품 검색
    @GetMapping("/search")
    public List<ProductResponseDto> searchProducts(@RequestParam String keyword) {
        List<Product> products = productRepository.findByNameContaining(keyword);
        return toResponseDtoList(products);
    }
    
    // 상품 정렬법
    @GetMapping("/sort")
    public List<ProductResponseDto> sortProducts(@RequestParam String sortType,
    		@RequestParam(required = false) String category, @RequestParam(required = false) String category2) {
    	
        List<Product> products;
        //낮은가격, 높은가격, 최신순
        switch (sortType) {
        	case "rating": // 베스트순 (리뷰평점순)
        		products = productRepository.findProductsOrderByRatingDesc(category);
            break;
            case "priceAsc":
                products = productRepository.findByCategoryFilterOrderByPriceAsc(category, category2);
                break;
            case "priceDesc":
                products = productRepository.findByCategoryFilterOrderByPriceDesc(category, category2);
                break;
            case "latest":
            	products = productRepository.findByCategoryFilterOrderByIdDesc(category, category2);
                break;
            default:
            	 products = productRepository.findAllByOrderByIdDesc();
                break;
        }

        return toResponseDtoList(products);
    }

    
    // 추가
    private List<ProductResponseDto> toResponseDtoListFromRating(List<ProductRatingDto> ratingList) {
        List<ProductResponseDto> result = new ArrayList<>();

        for (ProductRatingDto ratingDto : ratingList) {
            Product p = ratingDto.product(); // ProductRatingDto의 record 필드

            ProductResponseDto dto = new ProductResponseDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setPrice(p.getPrice());
            dto.setImagePath(p.getImagePath());
            dto.setImagePath2(p.getImagePath2());
            dto.setPrdDetail(p.getPrdDetail());
            dto.setPrdInfo(p.getPrdInfo());
            dto.setCategory(p.getCategory());
            dto.setCategory2(p.getCategory2());

            result.add(dto);
        }

        return result;
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @ModelAttribute ProductRequestDto dto, HttpSession session) throws IOException {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null || !member.isAdmin()) {
            return ResponseEntity.status(403).body("관리자만 수정 가능합니다.");
        }

        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Product product = optional.get();

        // 기존 이미지 삭제 후 교체
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            deleteImage(product.getImagePath());
        }
        if (dto.getImage2() != null && !dto.getImage2().isEmpty()) {
            deleteImage(product.getImagePath2());
        }

        applyDtoToProduct(dto, product);

        return ResponseEntity.ok(productRepository.save(product));
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null || !member.isAdmin()) {
            return ResponseEntity.status(403).body("관리자만 삭제 가능합니다.");
        }

        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Product product = optional.get();
        deleteImage(product.getImagePath());
        deleteImage(product.getImagePath2());
        productRepository.delete(product);

        return ResponseEntity.ok("삭제 완료");
    }

    // -------------------- 내부 메서드 --------------------
    private void applyDtoToProduct(ProductRequestDto dto, Product product) throws IOException {
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            product.setImagePath(saveImage(dto.getImage()));
        }
        if (dto.getImage2() != null && !dto.getImage2().isEmpty()) {
            product.setImagePath2(saveImage(dto.getImage2()));
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setPrdDetail(dto.getPrdDetail());
        product.setCategory(dto.getCategory());
        product.setCategory2(dto.getCategory2());

    }

    private String saveImage(MultipartFile file) throws IOException {
        File uploadDir = new File(uploadPath).getAbsoluteFile();
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);
        return "/images/" + fileName;
    }

    private void deleteImage(String path) {
        if (path != null) {
            File file = new File(uploadPath + path.replace("/images", ""));
            if (file.exists()) file.delete();
        }
    }

    private List<ProductResponseDto> toResponseDtoList(List<Product> products) {
        List<ProductResponseDto> result = new ArrayList<>();
        for (Product p : products) {
            ProductResponseDto dto = new ProductResponseDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setPrice(p.getPrice());
            dto.setImagePath(p.getImagePath());
            dto.setImagePath2(p.getImagePath2());
            dto.setPrdDetail(p.getPrdDetail());
            dto.setPrdInfo(p.getPrdInfo());
            dto.setCategory(p.getCategory());
            dto.setCategory2(p.getCategory2());
            result.add(dto);
        }
        return result;
    }
    //추가
 // ** 관리자: 총 등록 상품 수 반환
    @GetMapping("/admin/count")
    public ResponseEntity<?> getTotalProductCount(HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");

        if (member == null || !member.isAdmin()) {
            return ResponseEntity.status(403).body("관리자만 접근 가능");
        }

        long totalProducts = productRepository.count();

        return ResponseEntity.ok(totalProducts);
    }
}

package com.chtrembl.petstore.product.service;

import com.chtrembl.petstore.product.model.Product;
import com.chtrembl.petstore.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findProductsByStatus(List<String> statusList) {
        List<Product.StatusEnum> statuses = statusList.stream()
                .map(Product.StatusEnum::fromValue)
                .collect(Collectors.toList());
        return productRepository.findByStatusIn(statuses);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
}

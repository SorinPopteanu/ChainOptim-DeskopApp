package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.features.product.dto.CreateProductDTO;
import org.chainoptim.desktop.features.product.dto.UpdateProductDTO;
import org.chainoptim.desktop.features.product.model.Product;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductWriteService {

    CompletableFuture<Optional<Product>> createProduct(CreateProductDTO productDTO);

    // Update
    CompletableFuture<Optional<Product>> updateProduct(UpdateProductDTO updateProductDTO);

    // Delete
    CompletableFuture<Optional<Integer>> deleteProduct(Integer productId);
}

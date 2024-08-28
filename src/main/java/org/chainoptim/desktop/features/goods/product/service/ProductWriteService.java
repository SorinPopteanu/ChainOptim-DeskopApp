package org.chainoptim.desktop.features.goods.product.service;

import org.chainoptim.desktop.features.goods.product.dto.CreateProductDTO;
import org.chainoptim.desktop.features.goods.product.dto.UpdateProductDTO;
import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface ProductWriteService {

    CompletableFuture<Result<Product>> createProduct(CreateProductDTO productDTO);

    // Update
    CompletableFuture<Result<Product>> updateProduct(UpdateProductDTO updateProductDTO);

    // Delete
    CompletableFuture<Result<Integer>> deleteProduct(Integer productId);
}

package org.chainoptim.desktop;

import com.google.inject.AbstractModule;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.repository.UserRepositoryImpl;
import org.chainoptim.desktop.features.factory.repository.FactoryRepository;
import org.chainoptim.desktop.features.factory.repository.FactoryRepositoryImpl;
import org.chainoptim.desktop.features.product.repository.ProductRepository;
import org.chainoptim.desktop.features.product.repository.ProductRepositoryImpl;
import org.chainoptim.desktop.features.supplier.repository.SupplierRepository;
import org.chainoptim.desktop.features.supplier.repository.SupplierRepositoryImpl;
import org.chainoptim.desktop.features.warehouse.repository.WarehouseRepository;
import org.chainoptim.desktop.features.warehouse.repository.WarehouseRepositoryImpl;

/*
 * Module configuring Dependency Injections
 *
 */
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NavigationService.class).asEagerSingleton();

        bind(UserRepository.class).to(UserRepositoryImpl.class);
        bind(ProductRepository.class).to(ProductRepositoryImpl.class);
        bind(FactoryRepository.class).to(FactoryRepositoryImpl.class);
        bind(WarehouseRepository.class).to(WarehouseRepositoryImpl.class);
        bind(SupplierRepository.class).to(SupplierRepositoryImpl.class);
    }
}

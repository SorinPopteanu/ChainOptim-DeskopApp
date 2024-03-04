package org.chainoptim.desktop;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.abstraction.GuiceControllerFactory;
import org.chainoptim.desktop.core.abstraction.JavaFXThreadRunner;
import org.chainoptim.desktop.core.abstraction.ThreadRunner;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.repository.UserRepository;
import org.chainoptim.desktop.core.user.repository.UserRepositoryImpl;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.service.AuthenticationServiceImpl;
import org.chainoptim.desktop.features.factory.repository.FactoryRepository;
import org.chainoptim.desktop.features.factory.repository.FactoryRepositoryImpl;
import org.chainoptim.desktop.features.product.repository.ProductRepository;
import org.chainoptim.desktop.features.product.repository.ProductRepositoryImpl;
import org.chainoptim.desktop.features.supplier.repository.SupplierRepository;
import org.chainoptim.desktop.features.supplier.repository.SupplierRepositoryImpl;
import org.chainoptim.desktop.features.warehouse.repository.WarehouseRepository;
import org.chainoptim.desktop.features.warehouse.repository.WarehouseRepositoryImpl;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderServiceImpl;

import java.net.http.HttpClient;

/*
 * Module configuring Dependency Injections
 *
 */
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        // Singletons
        bind(NavigationServiceImpl.class).asEagerSingleton();
        bind(FallbackManager.class).in(Singleton.class);
        bind(CurrentSelectionService.class).in(Singleton.class);


        bind(HttpClient.class).toInstance(HttpClient.newHttpClient());

        // Bind interfaces to implementations
        // Services
        bind(ControllerFactory.class).to(GuiceControllerFactory.class);
        bind(ThreadRunner.class).to(JavaFXThreadRunner.class);
        bind(NavigationService.class).to(NavigationServiceImpl.class);
        bind(FXMLLoaderService.class).to(FXMLLoaderServiceImpl.class);

        // Repositories
        bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
        bind(UserRepository.class).to(UserRepositoryImpl.class);
        bind(ProductRepository.class).to(ProductRepositoryImpl.class);
        bind(FactoryRepository.class).to(FactoryRepositoryImpl.class);
        bind(WarehouseRepository.class).to(WarehouseRepositoryImpl.class);
        bind(SupplierRepository.class).to(SupplierRepositoryImpl.class);
    }
}

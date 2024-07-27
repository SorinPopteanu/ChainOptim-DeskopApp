package org.chainoptim.desktop;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.abstraction.GuiceControllerFactory;
import org.chainoptim.desktop.core.abstraction.JavaFXThreadRunner;
import org.chainoptim.desktop.core.abstraction.ThreadRunner;
import org.chainoptim.desktop.core.context.SupplyChainSnapshotContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.notification.controller.NotificationManager;
import org.chainoptim.desktop.core.notification.model.NotificationUser;
import org.chainoptim.desktop.core.notification.service.NotificationPersistenceService;
import org.chainoptim.desktop.core.notification.service.NotificationPersistenceServiceImpl;
import org.chainoptim.desktop.core.organization.service.CustomRoleService;
import org.chainoptim.desktop.core.organization.service.CustomRoleServiceImpl;
import org.chainoptim.desktop.core.organization.service.OrganizationService;
import org.chainoptim.desktop.core.organization.service.OrganizationServiceImpl;
import org.chainoptim.desktop.core.overview.service.SupplyChainSnapshotService;
import org.chainoptim.desktop.core.overview.service.SupplyChainSnapshotServiceImpl;
import org.chainoptim.desktop.core.settings.service.UserSettingsService;
import org.chainoptim.desktop.core.settings.service.UserSettingsServiceImpl;
import org.chainoptim.desktop.core.user.service.*;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.model.ClientOrder;
import org.chainoptim.desktop.features.client.model.ClientShipment;
import org.chainoptim.desktop.features.client.service.*;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.model.FactoryInventoryItem;
import org.chainoptim.desktop.features.factory.service.*;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.*;
import org.chainoptim.desktop.features.productpipeline.service.*;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphServiceImpl;
import org.chainoptim.desktop.features.scanalysis.productgraph.service.ProductProductionGraphService;
import org.chainoptim.desktop.features.scanalysis.productgraph.service.ProductProductionGraphServiceImpl;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryService;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryServiceImpl;
import org.chainoptim.desktop.features.scanalysis.productionperformance.service.FactoryPerformanceService;
import org.chainoptim.desktop.features.scanalysis.productionperformance.service.FactoryPerformanceServiceImpl;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationPersistenceServiceImpl;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.service.ResourceAllocationServiceImpl;
import org.chainoptim.desktop.features.scanalysis.supply.service.SupplierPerformanceService;
import org.chainoptim.desktop.features.scanalysis.supply.service.SupplierPerformanceServiceImpl;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.model.SupplierOrder;
import org.chainoptim.desktop.features.supplier.model.SupplierShipment;
import org.chainoptim.desktop.features.supplier.service.*;
import org.chainoptim.desktop.features.warehouse.model.Warehouse;
import org.chainoptim.desktop.features.warehouse.model.WarehouseInventoryItem;
import org.chainoptim.desktop.features.warehouse.service.*;
import org.chainoptim.desktop.shared.caching.CachingService;
import org.chainoptim.desktop.shared.caching.CachingServiceImpl;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.features.location.service.LocationService;
import org.chainoptim.desktop.shared.features.location.service.LocationServiceImpl;
import org.chainoptim.desktop.shared.httphandling.RequestBuilder;
import org.chainoptim.desktop.shared.httphandling.RequestBuilderImpl;
import org.chainoptim.desktop.shared.httphandling.RequestHandler;
import org.chainoptim.desktop.shared.httphandling.RequestHandlerImpl;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.search.model.SearchParamsImpl;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.controller.ToastManagerImpl;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoaderImpl;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import javafx.stage.Stage;

import java.net.http.HttpClient;


/**
 * Guice Module configuring Dependency Injections
 */
public class AppModule extends AbstractModule {

    private static Stage mainStage;

    @Override
    protected void configure() {
        // Singletons
        bind(NavigationServiceImpl.class).asEagerSingleton();
        bind(FallbackManager.class).in(Singleton.class);
        bind(CurrentSelectionService.class).in(Singleton.class);
        bind(NotificationManager.class).in(Singleton.class);
        bind(SupplyChainSnapshotContext.class).in(Singleton.class);
        bind(HttpClient.class).toInstance(HttpClient.newHttpClient());

        // Bind interfaces to implementations
        // Core
        // - Main
        bind(NavigationService.class).to(NavigationServiceImpl.class);

        // - Abstraction
        bind(ControllerFactory.class).to(GuiceControllerFactory.class);
        bind(ThreadRunner.class).to(JavaFXThreadRunner.class);
        bind(FXMLLoaderService.class).to(FXMLLoaderServiceImpl.class).in(Singleton.class);

        // - User
        bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
        bind(UserService.class).to(UserServiceImpl.class);
        bind(TokenManager.class).to(TokenManagerImpl.class).in(Singleton.class);

        // - Organization
        bind(OrganizationService.class).to(OrganizationServiceImpl.class);
        bind(CustomRoleService.class).to(CustomRoleServiceImpl.class);

        // - Overview
        bind(SupplyChainSnapshotService.class).to(SupplyChainSnapshotServiceImpl.class);

        // - Notifications
        bind(NotificationPersistenceService.class).to(NotificationPersistenceServiceImpl.class);

        // - Settings
        bind(UserSettingsService.class).to(UserSettingsServiceImpl.class);

        // Features
        // - Product
        bind(ProductService.class).to(ProductServiceImpl.class);
        bind(ProductWriteService.class).to(ProductWriteServiceImpl.class);
        bind(StageService.class).to(StageServiceImpl.class);
        bind(StageWriteService.class).to(StageWriteServiceImpl.class);
        bind(UnitOfMeasurementService.class).to(UnitOfMeasurementServiceImpl.class);

        // - Factory
        bind(FactoryService.class).to(FactoryServiceImpl.class);
        bind(FactoryWriteService.class).to(FactoryWriteServiceImpl.class);
        bind(FactoryStageService.class).to(FactoryStageServiceImpl.class);
        bind(FactoryStageWriteService.class).to(FactoryStageWriteServiceImpl.class);
        bind(FactoryInventoryItemService.class).to(FactoryInventoryItemServiceImpl.class);
        bind(FactoryInventoryItemWriteService.class).to(FactoryInventoryItemWriteServiceImpl.class);

        // - Warehouse
        bind(WarehouseService.class).to(WarehouseServiceImpl.class);
        bind(WarehouseWriteService.class).to(WarehouseWriteServiceImpl.class);
        bind(WarehouseInventoryItemService.class).to(WarehouseInventoryItemServiceImpl.class);
        bind(WarehouseInventoryItemWriteService.class).to(WarehouseInventoryItemWriteServiceImpl.class);
        bind(CompartmentService.class).to(CompartmentServiceImpl.class);

        // - Supplier
        bind(SupplierService.class).to(SupplierServiceImpl.class);
        bind(SupplierWriteService.class).to(SupplierWriteServiceImpl.class);
        bind(SupplierOrdersService.class).to(SupplierOrdersServiceImpl.class);
        bind(SupplierOrdersWriteService.class).to(SupplierOrdersWriteServiceImpl.class);
        bind(SupplierShipmentsService.class).to(SupplierShipmentsServiceImpl.class);
        bind(SupplierShipmentsWriteService.class).to(SupplierShipmentsWriteServiceImpl.class);

        // - Components
        bind(ComponentService.class).to(ComponentServiceImpl.class);

        // - Client
        bind(ClientService.class).to(ClientServiceImpl.class);
        bind(ClientWriteService.class).to(ClientWriteServiceImpl.class);
        bind(ClientOrdersService.class).to(ClientOrdersServiceImpl.class);
        bind(ClientOrdersWriteService.class).to(ClientOrdersWriteServiceImpl.class);
        bind(ClientShipmentsService.class).to(ClientShipmentsServiceImpl.class);
        bind(ClientShipmentsWriteService.class).to(ClientShipmentsWriteServiceImpl.class);

        // - SC Analysis
        bind(ProductProductionGraphService.class).to(ProductProductionGraphServiceImpl.class);
        bind(FactoryProductionGraphService.class).to(FactoryProductionGraphServiceImpl.class);
        bind(ResourceAllocationService.class).to(ResourceAllocationServiceImpl.class);
        bind(ResourceAllocationPersistenceService.class).to(ResourceAllocationPersistenceServiceImpl.class);

        bind(FactoryProductionHistoryService.class).to(FactoryProductionHistoryServiceImpl.class);
        bind(FactoryPerformanceService.class).to(FactoryPerformanceServiceImpl.class);
        bind(SupplierPerformanceService.class).to(SupplierPerformanceServiceImpl.class);

        // Shared
        // - Http Handling
        bind(RequestBuilder.class).to(RequestBuilderImpl.class);
        bind(RequestHandler.class).to(RequestHandlerImpl.class);

        // - Location
        bind(LocationService.class).to(LocationServiceImpl.class);

        // - Resource Loading
        bind(CommonViewsLoader.class).to(CommonViewsLoaderImpl.class);

        // - Search
        bind(SearchParams.class).to(SearchParamsImpl.class);

        // - Toast
        bind(ToastManager.class).to(ToastManagerImpl.class);

        // - Caching
        bind(new TypeLiteral<CachingService<PaginatedResults<NotificationUser>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<NotificationUser>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<Product>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<Product>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<Factory>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<Factory>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<FactoryInventoryItem>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<FactoryInventoryItem>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<Warehouse>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<Warehouse>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<WarehouseInventoryItem>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<WarehouseInventoryItem>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<Supplier>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<Supplier>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<SupplierOrder>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<SupplierOrder>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<SupplierShipment>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<SupplierShipment>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<Client>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<Client>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<ClientOrder>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<ClientOrder>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<PaginatedResults<ClientShipment>>>() {})
                .to(new TypeLiteral<CachingServiceImpl<PaginatedResults<ClientShipment>>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<ResourceAllocationPlan>>() {})
                .to(new TypeLiteral<CachingServiceImpl<ResourceAllocationPlan>>() {})
                .in(Singleton.class);
        bind(new TypeLiteral<CachingService<FactoryProductionHistory>>() {})
                .to(new TypeLiteral<CachingServiceImpl<FactoryProductionHistory>>() {})
                .in(Singleton.class);
    }

    public static void setStage(Stage mainStage) {
        AppModule.mainStage = mainStage;
    }

    @Provides
    public Stage provideStage() {
        if (mainStage == null) {
            throw new IllegalStateException("Stage has not been initialized.");
        }
        return mainStage;
    }
}

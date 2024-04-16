package org.chainoptim.desktop.shared.common.uielements.info;

import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.InfoLevel;

import java.util.EnumMap;

public class FeatureInfoMapper {

    private FeatureInfoMapper() {}

    private static final EnumMap<Feature, FeatureInfo> featureInfoMap = new EnumMap<>(Feature.class);

    static {
        // Products
        featureInfoMap.put(Feature.PRODUCT, new FeatureInfo(
                "A Product is any item that is manufactured and/or sold by your organization.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.PRODUCT_STAGE, new FeatureInfo(
                "A Product Stage is any step in the manufacturing process of a product. " +
                "You can define any number of Stage Inputs and Stage Outputs, consisting of " +
                "Components and how much of them are needed or produced in one full Stage. " +
                "You can also set up Connections between them, effectively configuring a 'Production Pipeline'.",
                InfoLevel.ADVANCED));
        featureInfoMap.put(Feature.COMPONENT, new FeatureInfo(
                "A Component is any good that is used in the manufacturing process of a product.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.UNIT_OF_MEASUREMENT, new FeatureInfo(
                "A Unit of Measurement is any unit that is used to measure quantities of Products or Components.",
                InfoLevel.ALL));

        // Factories
        featureInfoMap.put(Feature.FACTORY, new FeatureInfo(
                "A Factory is any organization site where manufacturing takes place.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.FACTORY_STAGE, new FeatureInfo(
                "A Factory Stage is any manufacturing process taking place in a Factory. " +
                "You can define the underlying Product Stage, " +
                "the number of stages the Factory can execute (Capacity) in a given time period (Duration), " +
                "as well as priorities for allocation of resources. " +
                "You can also set up Connections between them, determining a continuous flow of production.",
                InfoLevel.ADVANCED));
        featureInfoMap.put(Feature.RESOURCE_ALLOCATION_PLAN, new FeatureInfo(
                "A Resource Allocation Plan is a plan that determines how resources are allocated to Factory Stages. " +
                "You can compute an Allocation Plan over a time period, based on the current Factory Inventory levels, " +
                "activate it, as well as seek resources for existing deficits.",
                InfoLevel.ADVANCED));
        featureInfoMap.put(Feature.FACTORY_INVENTORY, new FeatureInfo(
                "A Factory Inventory is a record of all the Products and Components currently stored within a Factory.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.FACTORY_PRODUCTION_HISTORY, new FeatureInfo(
                "A Factory Production History is a record of all the Factory Stages executed by a Factory over time. " +
                "You can add records over any given time period, along with the planned allocations during that time. " +
                "The more records you add, the better the system can analyze your production performance and provide insights into optimizing it.",
                InfoLevel.ADVANCED));
        featureInfoMap.put(Feature.FACTORY_PERFORMANCE, new FeatureInfo(
                "A Factory Performance Report is a detailed analysis of the Production Performance of a Factory over time. " +
                "It measures the efficiency of resource distribution, readiness and utilization, as well as several other key performance indicators. " +
                "You can refresh the report at any time with the most up-to-date Factory Production History.",
                InfoLevel.ALL));

        // Warehouses
        featureInfoMap.put(Feature.WAREHOUSE, new FeatureInfo(
                "A Warehouse is any organization site where storage and distribution of goods takes place.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.WAREHOUSE_INVENTORY, new FeatureInfo(
                "A Warehouse Inventory is a record of all the Products and Components currently stored within a Warehouse.",
                InfoLevel.ALL));

        // Supplier
        featureInfoMap.put(Feature.SUPPLIER, new FeatureInfo(
                "A Supplier is any organization that provides goods to your organization.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.SUPPLIER_ORDER, new FeatureInfo(
                "A Supplier Order is a request for goods from a Supplier. " +
                "You can create an Order for any Supplier, specifying the Product or Component you need, as well as the quantities. " +
                "You can also set up a Delivery Date and a Delivery Location.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.SUPPLIER_SHIPMENT, new FeatureInfo(
                "A Supplier Shipment is any delivery of goods from a Supplier to your organization. " +
                "You can create a Shipment for any Supplier Order, specifying the quantities delivered, as well as the actual Delivery Date.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.SUPPLIER_PERFORMANCE, new FeatureInfo(
                "A Supplier Performance Report is a detailed analysis of the Performance of a Supplier over time. " +
                "It measures the reliability of deliveries, the quality of goods, as well as several other key performance indicators. " +
                "You can refresh the report at any time with the most up-to-date Supplier Orders.",
                InfoLevel.ALL));


        // Clients
        featureInfoMap.put(Feature.CLIENT, new FeatureInfo(
                "A Client is any organization that receives goods from your organization.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.CLIENT_ORDER, new FeatureInfo(
                "A Client Order is a request for goods from a Client. " +
                "You can create an Order for any Client, specifying the Product you need, as well as the quantities. " +
                "You can also set up a Delivery Date and a Delivery Location.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.CLIENT_SHIPMENT, new FeatureInfo(
                "A Client Shipment is any delivery of goods from your organization to a Client. " +
                "You can create a Shipment for any Client Order, specifying the quantities delivered, as well as the actual Delivery Date.",
                InfoLevel.ALL));
        featureInfoMap.put(Feature.CLIENT_EVALUATION, new FeatureInfo(
                "A Client Evaluation Report is a detailed analysis of the Client over time. " +
                "It measures the demand, reliability, as well as several other key performance indicators. " +
                "You can refresh the report at any time with the most up-to-date Client Orders.",
                InfoLevel.ALL));
    }

    public static FeatureInfo getFeatureInfo(Feature feature) {
        return featureInfoMap.getOrDefault(feature, null);
    }
}

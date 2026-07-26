package com.ferreteria_edu.ferreteria_api.order.dto;

import lombok.Data;

@Data
public class ImportSalesResultDTO {

    private int importedOrders;

    private int importedItems;

    private int errors;

}

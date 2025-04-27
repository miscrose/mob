export interface StockLot {
    id: number;
    quantity: number;
    expirationDate: string;
}

export interface MedicationStock {
    id: number;
    name: string;
    imageUrl: string | null;
    totalQuantity: number;
    lots: StockLot[];
} 
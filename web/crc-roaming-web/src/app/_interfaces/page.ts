export interface Page<T> {
    content: T[];
    pageSize: number;
    pageNumber: number;
    totalElements: number;
    fromRow: number;
    toRow: number;
}
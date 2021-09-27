export class SortingPaging {
    pageSize?: number;
    pageNumber?: number;
    totalElements?: number;
    fromRow?: number;
    toRow?: number;

    private sortBy?: string;
    private sortDirection?: string;

     public constructor (pageSize: number) {
       this.pageSize = pageSize;
       this.pageNumber = 1;
     }
   
     public isSortingValid(): boolean {
       if (this.sortBy && this.sortDirection) {
           return true;
       }
       return false;
     }
   
     public getPageSize(): string {
         if (this.pageSize == null) {
             return "";
         } else {
           return this.pageSize.toString();
         }
     }
   
     public getPageNumber(): string {
       if (this.pageNumber == null) {
           return "";
       } else {
         return this.pageNumber.toString();
       }
     }
   
     public getSortBy(): string {
       if (!this.sortBy) {
           return "";
       } else {
         return this.sortBy;
       }
     }
   
     public setSortBy(sortBy: string): void {
         this.sortBy = sortBy;
     }
   
     public getSortDirection(): string {
       if (!this.sortDirection) {
           return "";
       } else {
         return this.sortDirection;
       }
     }
   
     public setSortDirection(sortDirection: string): void {
         this.sortDirection = sortDirection;
     }
   
   }
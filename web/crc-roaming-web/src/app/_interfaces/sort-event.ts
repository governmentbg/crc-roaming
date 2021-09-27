export interface SortEvent {
    column: string;
    direction: SortDirection;
}

export type SortDirection = 'asc' | 'desc' | '';
export const rotate: {[key: string]: SortDirection} = { 'asc': 'desc', 'desc': '', '': 'asc' };
export const compare = (v1, v2) => v1 < v2 ? -1 : v1 > v2 ? 1 : 0;
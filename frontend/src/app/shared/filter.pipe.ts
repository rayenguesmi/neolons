import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {
  
  transform(items: any[], searchField: string, searchValue: any): any[] {
    if (!items || !searchField || searchValue === undefined || searchValue === null || searchValue === '') {
      return items;
    }

    return items.filter(item => {
      const fieldValue = this.getNestedProperty(item, searchField);
      
      if (fieldValue === undefined || fieldValue === null) {
        return false;
      }

      // Si searchValue est un boolean
      if (typeof searchValue === 'boolean') {
        return fieldValue === searchValue;
      }

      // Si searchValue est un string
      if (typeof searchValue === 'string') {
        // Si le champ est un array (comme roles)
        if (Array.isArray(fieldValue)) {
          return fieldValue.some(val => 
            val.toString().toLowerCase().includes(searchValue.toLowerCase())
          );
        }
        
        return fieldValue.toString().toLowerCase().includes(searchValue.toLowerCase());
      }

      // Si searchValue est un array
      if (Array.isArray(searchValue)) {
        if (Array.isArray(fieldValue)) {
          return searchValue.some(val => fieldValue.includes(val));
        }
        return searchValue.includes(fieldValue);
      }

      return fieldValue === searchValue;
    });
  }

  private getNestedProperty(obj: any, path: string): any {
    return path.split('.').reduce((current, prop) => current && current[prop], obj);
  }
}


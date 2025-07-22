import { Component, Input, OnInit } from '@angular/core';
import { AuditChange, AuditLogExtended } from '../../models/audit-change.model';

@Component({
  selector: 'app-audit-diff-viewer',
  templateUrl: './audit-diff-viewer.component.html',
  styleUrls: ['./audit-diff-viewer.component.css']
})
export class AuditDiffViewerComponent implements OnInit {
  @Input() auditLog: AuditLogExtended | null = null;
  @Input() changes: AuditChange[] = [];
  @Input() showFullDiff = false;

  expandedChanges: Set<string> = new Set();
  viewMode: 'side-by-side' | 'unified' = 'side-by-side';

  ngOnInit(): void {
    // Auto-expand critical changes
    this.changes.forEach(change => {
      if (this.isCriticalField(change.fieldName)) {
        this.expandedChanges.add(change.id);
      }
    });
  }

  toggleChange(changeId: string): void {
    if (this.expandedChanges.has(changeId)) {
      this.expandedChanges.delete(changeId);
    } else {
      this.expandedChanges.add(changeId);
    }
  }

  isExpanded(changeId: string): boolean {
    return this.expandedChanges.has(changeId);
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'side-by-side' ? 'unified' : 'side-by-side';
  }

  expandAll(): void {
    this.changes.forEach(change => {
      this.expandedChanges.add(change.id);
    });
  }

  collapseAll(): void {
    this.expandedChanges.clear();
  }

  getChangeTypeClass(change: AuditChange): string {
    if (!change.oldValue && change.newValue) {
      return 'change-added';
    }
    if (change.oldValue && !change.newValue) {
      return 'change-removed';
    }
    if (change.oldValue !== change.newValue) {
      return 'change-modified';
    }
    return 'change-unchanged';
  }

  getFieldDisplayName(fieldName: string): string {
    const fieldMappings: { [key: string]: string } = {
      'name': 'Nom',
      'email': 'Email',
      'status': 'Statut',
      'role': 'Rôle',
      'permissions': 'Permissions',
      'description': 'Description',
      'title': 'Titre',
      'content': 'Contenu',
      'tags': 'Étiquettes',
      'category': 'Catégorie',
      'priority': 'Priorité',
      'assignee': 'Assigné à',
      'dueDate': 'Date d\'échéance',
      'createdAt': 'Date de création',
      'updatedAt': 'Date de modification'
    };
    return fieldMappings[fieldName] || fieldName;
  }

  isCriticalField(fieldName: string): boolean {
    const criticalFields = ['permissions', 'role', 'status', 'password', 'email'];
    return criticalFields.includes(fieldName);
  }

  formatValue(value: string, dataType: string): string {
    if (!value) return '(vide)';
    
    try {
      switch (dataType) {
        case 'json':
          return JSON.stringify(JSON.parse(value), null, 2);
        case 'date':
          return new Date(value).toLocaleString('fr-FR');
        case 'boolean':
          return value === 'true' ? 'Oui' : 'Non';
        case 'array':
          const array = JSON.parse(value);
          return Array.isArray(array) ? array.join(', ') : value;
        default:
          return value;
      }
    } catch {
      return value;
    }
  }

  getValuePreview(value: string, maxLength: number = 50): string {
    const formatted = this.formatValue(value, 'string');
    if (formatted.length <= maxLength) {
      return formatted;
    }
    return formatted.substring(0, maxLength) + '...';
  }

  highlightDifferences(oldValue: string, newValue: string): { old: string; new: string } {
    // Simple word-level diff highlighting
    const oldWords = oldValue.split(/\s+/);
    const newWords = newValue.split(/\s+/);
    
    let oldHighlighted = '';
    let newHighlighted = '';
    
    const maxLength = Math.max(oldWords.length, newWords.length);
    
    for (let i = 0; i < maxLength; i++) {
      const oldWord = oldWords[i] || '';
      const newWord = newWords[i] || '';
      
      if (oldWord !== newWord) {
        if (oldWord) {
          oldHighlighted += `<span class="diff-removed">${oldWord}</span> `;
        }
        if (newWord) {
          newHighlighted += `<span class="diff-added">${newWord}</span> `;
        }
      } else {
        oldHighlighted += oldWord + ' ';
        newHighlighted += newWord + ' ';
      }
    }
    
    return {
      old: oldHighlighted.trim(),
      new: newHighlighted.trim()
    };
  }

  exportDiff(): void {
    const diffContent = this.generateDiffText();
    const blob = new Blob([diffContent], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-diff-${this.auditLog?.id}-${new Date().toISOString().split('T')[0]}.txt`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private generateDiffText(): string {
    let content = '';
    
    if (this.auditLog) {
      content += `Audit Log Diff Report\n`;
      content += `======================\n\n`;
      content += `Log ID: ${this.auditLog.id}\n`;
      content += `User: ${this.auditLog.username} (${this.auditLog.userId})\n`;
      content += `Action: ${this.auditLog.action}\n`;
      content += `Resource: ${this.auditLog.resourceType} (${this.auditLog.resourceId})\n`;
      content += `Timestamp: ${new Date(this.auditLog.timestamp).toLocaleString('fr-FR')}\n`;
      content += `\nChanges:\n`;
      content += `--------\n\n`;
    }
    
    this.changes.forEach(change => {
      content += `Field: ${this.getFieldDisplayName(change.fieldName)}\n`;
      content += `Old Value: ${this.formatValue(change.oldValue, change.dataType)}\n`;
      content += `New Value: ${this.formatValue(change.newValue, change.dataType)}\n`;
      content += `\n`;
    });
    
    return content;
  }

  getChangeIcon(change: AuditChange): string {
    if (!change.oldValue && change.newValue) {
      return 'fa-plus';
    }
    if (change.oldValue && !change.newValue) {
      return 'fa-minus';
    }
    if (change.oldValue !== change.newValue) {
      return 'fa-edit';
    }
    return 'fa-equals';
  }

  getChangeSummary(): { added: number; removed: number; modified: number } {
    const summary = { added: 0, removed: 0, modified: 0 };
    
    this.changes.forEach(change => {
      if (!change.oldValue && change.newValue) {
        summary.added++;
      } else if (change.oldValue && !change.newValue) {
        summary.removed++;
      } else if (change.oldValue !== change.newValue) {
        summary.modified++;
      }
    });
    
    return summary;
  }
}


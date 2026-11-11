import {Component, Input, OnDestroy, signal} from '@angular/core';

@Component({
  selector: 'app-document-viewer',
  imports: [],
  templateUrl: './document-viewer.html',
  styleUrl: './document-viewer.scss',
})
export class DocumentViewer implements OnDestroy {
  private documentData: string | null = null;
  private rawBlobUrl: string | null = null;

  @Input() filename = 'document';
  @Input() contentType: string | null = null;
  errorMessage = signal<string | null>(null);

  @Input()
  set pdfBase64(value: string | null | undefined) {
    this.documentData = value?.trim() || null;
    this.errorMessage.set(null);
    this.releaseBlobUrl();
  }

  get hasDocument(): boolean {
    return this.documentData !== null;
  }

  ngOnDestroy(): void {
    this.releaseBlobUrl();
  }

  telechargerDocument(): void {
    if (!this.creerUrlDocument()) {
      return;
    }

    const link = document.createElement('a');
    link.href = this.rawBlobUrl!;
    link.download = this.filename || 'document';
    document.body.appendChild(link);
    link.click();
    link.remove();
  }

  imprimerDocument(): void {
    if (!this.creerUrlDocument()) {
      return;
    }

    const iframe = document.createElement('iframe');
    iframe.title = `Impression : ${this.filename}`;
    iframe.style.position = 'fixed';
    iframe.style.width = '1px';
    iframe.style.height = '1px';
    iframe.style.opacity = '0';
    iframe.src = this.rawBlobUrl!;
    document.body.appendChild(iframe);

    iframe.onload = () => {
      iframe.contentWindow?.focus();
      iframe.contentWindow?.print();
      window.setTimeout(() => iframe.remove(), 1000);
    };
  }

  private creerUrlDocument(): boolean {
    if (this.rawBlobUrl) {
      return true;
    }
    if (!this.documentData) {
      this.errorMessage.set('Aucun fichier disponible pour cet ordre de mission.');
      return false;
    }

    try {
      const dataUrlMatch = this.documentData.match(/^data:([^;,]+);base64,(.*)$/s);
      const encodedData = (dataUrlMatch ? dataUrlMatch[2] : this.documentData)
        .replace(/\s/g, '')
        .replace(/-/g, '+')
        .replace(/_/g, '/');
      const binary = atob(encodedData);
      const bytes = new Uint8Array(binary.length);
      for (let index = 0; index < binary.length; index++) {
        bytes[index] = binary.charCodeAt(index);
      }

      const mimeType = dataUrlMatch?.[1] || this.contentType || this.mimeFromFilename();
      this.rawBlobUrl = URL.createObjectURL(new Blob([bytes], {type: mimeType}));
      this.errorMessage.set(null);
      return true;
    } catch (error) {
      console.error('Impossible de décoder le document de mission.', error);
      this.errorMessage.set('Le document est illisible ou son format est invalide.');
      return false;
    }
  }

  private mimeFromFilename(): string {
    const extension = this.filename.split('.').pop()?.toLowerCase();
    if (extension === 'pdf') return 'application/pdf';
    if (extension === 'png') return 'image/png';
    if (extension === 'jpg' || extension === 'jpeg') return 'image/jpeg';
    if (extension === 'gif') return 'image/gif';
    if (extension === 'webp') return 'image/webp';
    return 'application/octet-stream';
  }

  private releaseBlobUrl(): void {
    if (this.rawBlobUrl) {
      URL.revokeObjectURL(this.rawBlobUrl);
      this.rawBlobUrl = null;
    }
  }
}

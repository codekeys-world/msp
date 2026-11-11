import {Component, EventEmitter, input, Output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {OrdreMissionResponse} from '../models/ordre-mission-response';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-ordre-mission-details',
  imports: [
    DocumentViewer,
    DatePipe
  ],
  templateUrl: './ordre-mission-details.html',
  styleUrl: './ordre-mission-details.scss',
})
export class OrdreMissionDetails {
  ordreMission = input.required<OrdreMissionResponse>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);

  @Output() edit = new EventEmitter<OrdreMissionResponse>();
  @Output() delete = new EventEmitter<OrdreMissionResponse>();
}

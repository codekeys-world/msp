import {OrdreMissionResponse} from './ordre-mission-response';

export interface PageResponseOrdreMissionResponse {
  content?: Array<OrdreMissionResponse>;
  first?: boolean;
  last?: boolean;
  number?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
}

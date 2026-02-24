export interface ExtractionCreatedResponse {
  id: string;
  sourceFileName: string;
  extractedAt: string;
  berufeCount: number;
}

export interface ExtractionSummaryResponse {
  id: string;
  sourceFileName: string;
  extractedAt: string;
  berufeCount: number;
}

export interface ExtractionDetailResponse {
  id: string;
  sourceFileName: string;
  extractedAt: string;
  berufe: BerufResponse[];
  verification: VerificationResultResponse | null;
}

export interface BerufResponse {
  beschreibung: string;
  berufNr: number[];
  pruefungsBereich: PruefungsBereichResponse[];
}

export interface PruefungsBereichResponse {
  name: string;
  aufgaben: AufgabeResponse[];
}

export interface AufgabeResponse {
  name: string;
  struktur: string;
  termin: TerminResponse;
  hilfmittel: string;
}

export interface TerminResponse {
  datum: string;
  uhrzeitVon: string;
  uhrzeitBis: string;
  dauer: number;
}

export interface VerificationResultResponse {
  valid: boolean;
  issues: VerificationIssueResponse[];
  verifiedAt: string;
}

export interface VerificationIssueResponse {
  severity: "ERROR" | "WARNING" | "INFO";
  field: string;
  message: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

export interface AppSettings {
  anthropicApiKey: string;
  anthropicBaseUrl: string;
}

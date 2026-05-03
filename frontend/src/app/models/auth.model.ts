export interface LoginDto {
  email: string;
  password: string;
}

export interface RegisterDto {
  email: string;
  username: string;
  password: string;
}

export interface LoginResponseDto {
  access_token: string;
  token_type: string;
  expires_in: number;
}

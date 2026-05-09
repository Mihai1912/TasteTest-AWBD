export interface UserAdminDto {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface FeedbackAdminDto {
  id: string;
  feedbackType: string;
  experience: string;
  comment: string;
}


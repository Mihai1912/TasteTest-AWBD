import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AgentService } from '../../services/agent.service';

interface ChatTurn {
  role: 'user' | 'assistant' | 'error';
  text: string;
}

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './assistant.html',
  styleUrls: ['./assistant.css'],
})
export class Assistant {
  turns: ChatTurn[] = [];
  draft = '';
  loading = false;

  readonly suggestions: string[] = [
    'What restaurants are available?',
    'Show me the top-rated places.',
    "What's the rating of <restaurant name>?",
  ];

  constructor(private agentService: AgentService) {}

  send() {
    const message = this.draft.trim();
    if (!message || this.loading) return;
    this.draft = '';
    this.turns.push({ role: 'user', text: message });
    this.loading = true;
    this.agentService.chat(message).subscribe({
      next: (response) => {
        this.turns.push({ role: 'assistant', text: response.reply });
        this.loading = false;
      },
      error: (err) => {
        const detail = err?.error?.reply || err?.message || 'Unknown error';
        this.turns.push({ role: 'error', text: 'TasteBot is unavailable: ' + detail });
        this.loading = false;
      },
    });
  }

  pick(suggestion: string) {
    this.draft = suggestion;
  }
}

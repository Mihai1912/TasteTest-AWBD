import { Component, ElementRef, ViewChild, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AgentService } from '../../services/agent.service';

interface ChatTurn {
  role: 'user' | 'assistant' | 'error';
  text: string;
  html?: SafeHtml;
}

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './assistant.html',
  styleUrls: ['./assistant.css'],
})
export class Assistant implements AfterViewChecked {
  @ViewChild('chatWindow') private chatWindow!: ElementRef<HTMLDivElement>;

  turns: ChatTurn[] = [];
  draft = '';
  loading = false;
  private shouldScroll = false;

  readonly suggestions: string[] = [
    'What restaurants are available?',
    'Show me the top-rated places.',
    "What's the rating of <restaurant name>?",
  ];

  constructor(
    private agentService: AgentService,
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef,
  ) {}

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom() {
    try {
      const el = this.chatWindow.nativeElement;
      el.scrollTop = el.scrollHeight;
    } catch (_) {}
  }

  send() {
    const message = this.draft.trim();
    if (!message || this.loading) return;
    this.draft = '';
    this.turns.push({ role: 'user', text: message });
    this.shouldScroll = true;
    this.loading = true;
    this.agentService.chat(message).subscribe({
      next: (response) => {
        const reply = response?.reply ?? '';
        this.turns.push({
          role: 'assistant',
          text: reply,
          html: this.sanitizer.bypassSecurityTrustHtml(this.renderMarkdown(reply)),
        });
        this.loading = false;
        this.shouldScroll = true;
        // Zoneless app: async callbacks don't auto-trigger change detection,
        // so we must notify Angular to re-render (hide the typing dots, show the reply).
        try { this.cdr.detectChanges(); } catch (_) {}
      },
      error: (err) => {
        const detail = err?.error?.reply || err?.message || 'Unknown error';
        this.turns.push({ role: 'error', text: 'TasteBot is unavailable: ' + detail });
        this.loading = false;
        this.shouldScroll = true;
        try { this.cdr.detectChanges(); } catch (_) {}
      },
    });
  }

  pick(suggestion: string) {
    this.draft = suggestion;
  }

  // Minimal markdown -> HTML converter for the subset the agent emits:
  // **bold**, *italic*, `code`, "- " bullets, numbered lists, links, and
  // paragraph/newline handling. Output is HTML-escaped first so user/model
  // input cannot inject tags.
  private renderMarkdown(input: string): string {
    const esc = (s: string) =>
      s
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');

    const text = esc(input);
    const lines = text.split(/\r?\n/);

    const out: string[] = [];
    let inUl = false;
    let inOl = false;
    const closeLists = () => {
      if (inUl) { out.push('</ul>'); inUl = false; }
      if (inOl) { out.push('</ol>'); inOl = false; }
    };

    const inline = (s: string): string =>
      s
        // links: [text](url)
        .replace(
          /\[([^\]]+)\]\((https?:[^\s)]+)\)/g,
          '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>',
        )
        // bold: **text**
        .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
        // italic: *text* (not greedy, avoids eating across bold)
        .replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>')
        // inline code: `text`
        .replace(/`([^`]+)`/g, '<code>$1</code>');

    for (const raw of lines) {
      const line = raw.trimEnd();
      const bulletMatch = /^\s*[-*•]\s+(.*)$/.exec(line);
      const orderedMatch = /^\s*\d+[.)]\s+(.*)$/.exec(line);

      if (bulletMatch) {
        if (inOl) { out.push('</ol>'); inOl = false; }
        if (!inUl) { out.push('<ul>'); inUl = true; }
        out.push(`<li>${inline(bulletMatch[1])}</li>`);
      } else if (orderedMatch) {
        if (inUl) { out.push('</ul>'); inUl = false; }
        if (!inOl) { out.push('<ol>'); inOl = true; }
        out.push(`<li>${inline(orderedMatch[1])}</li>`);
      } else if (line.trim() === '') {
        closeLists();
        out.push('');
      } else if (/^#{1,6}\s+/.test(line)) {
        closeLists();
        const m = /^(#{1,6})\s+(.*)$/.exec(line)!;
        const level = m[1].length;
        out.push(`<h${level}>${inline(m[2])}</h${level}>`);
      } else {
        closeLists();
        out.push(`<p>${inline(line)}</p>`);
      }
    }
    closeLists();
    return out.join('\n');
  }
}

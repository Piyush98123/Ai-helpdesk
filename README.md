📋 Overview
An intelligent, agentic AI helpdesk system that automates IT support ticket management. Built with Spring AI and Ollama, this system leverages Large Language Models to understand user queries, extract relevant information, and autonomously perform business actions like creating tickets, assigning technicians, and sending notifications.

🤖 What Makes This Agentic AI?
Unlike traditional chatbots that only provide responses, this system acts on user requests:

Tool Calling - The AI autonomously decides when to create tickets, assign priorities, and trigger emails

Memory & State - Full conversation history stored in PostgreSQL for context-aware interactions

Multi-Step Workflow - Executes complex business processes without human intervention

Reasoning + Action - Implements the ReAct (Reason + Act) pattern for intelligent decision making

✨ Features
Feature	Description
🧠 LLM Integration	Local LLM via Ollama (Llama 2, Mistral, or Phi)
🎯 Tool/Function Calling	AI autonomously invokes business functions
🎫 Automated Ticket Creation	Creates incidents with proper priority & status
👤 Intelligent Assignment	Auto-assigns tickets to available technicians
📧 Email Notifications	Sends technician details to users automatically
💾 Conversation Memory	Full chat history stored in PostgreSQL
🔌 REST API	Easy integration with external systems

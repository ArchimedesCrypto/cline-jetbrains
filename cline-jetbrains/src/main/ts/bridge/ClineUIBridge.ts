import { ClineBridge, ClineSettings, TaskOptions, TaskResult, TaskStatus } from './ClineBridge';

/**
 * Interface for UI state.
 */
export interface UIState {
    mode: string;
    model: string;
    theme: string;
    fontSize: number;
    autoApprove: boolean;
    taskStatus: TaskStatus;
    showSettings: boolean;
    showHistory: boolean;
    showMcp: boolean;
    showAccount: boolean;
}

/**
 * Interface for UI event handlers.
 */
export interface UIEventHandlers {
    onModeChange: (mode: string) => void;
    onModelChange: (model: string) => void;
    onThemeChange: (theme: string) => void;
    onFontSizeChange: (fontSize: number) => void;
    onAutoApproveChange: (autoApprove: boolean) => void;
    onSettingsToggle: () => void;
    onHistoryToggle: () => void;
    onMcpToggle: () => void;
    onAccountToggle: () => void;
    onTaskSubmit: (task: string, options?: TaskOptions) => void;
    onTaskCancel: () => void;
}

/**
 * Class that handles communication between the Java code and the React UI.
 */
export class ClineUIBridge {
    private bridge: ClineBridge;
    private state: UIState;
    private eventHandlers: UIEventHandlers | undefined;

    constructor(bridge: ClineBridge) {
        this.bridge = bridge;
        this.state = {
            mode: 'ask',
            model: 'gpt-4',
            theme: 'dark',
            fontSize: 14,
            autoApprove: false,
            taskStatus: { status: 'idle', message: 'No task running' },
            showSettings: false,
            showHistory: false,
            showMcp: false,
            showAccount: false
        };
    }

    /**
     * Initialize the UI bridge.
     * @param settings The settings
     */
    async initialize(settings: ClineSettings): Promise<void> {
        // Update state with settings
        this.state = {
            ...this.state,
            mode: settings.mode || this.state.mode,
            model: settings.model || this.state.model,
            theme: settings.theme || this.state.theme,
            fontSize: settings.fontSize || this.state.fontSize,
            autoApprove: settings.autoApprove || this.state.autoApprove
        };

        // Notify UI of state change
        this.notifyStateChange();
    }

    /**
     * Set the event handlers.
     * @param handlers The event handlers
     */
    setEventHandlers(handlers: UIEventHandlers): void {
        this.eventHandlers = handlers;
    }

    /**
     * Handle a mode change.
     * @param mode The new mode
     */
    handleModeChange(mode: string): void {
        this.state.mode = mode;
        this.notifyStateChange();
        this.eventHandlers?.onModeChange(mode);
    }

    /**
     * Handle a model change.
     * @param model The new model
     */
    handleModelChange(model: string): void {
        this.state.model = model;
        this.notifyStateChange();
        this.eventHandlers?.onModelChange(model);
    }

    /**
     * Handle a theme change.
     * @param theme The new theme
     */
    handleThemeChange(theme: string): void {
        this.state.theme = theme;
        this.notifyStateChange();
        this.eventHandlers?.onThemeChange(theme);
    }

    /**
     * Handle a font size change.
     * @param fontSize The new font size
     */
    handleFontSizeChange(fontSize: number): void {
        this.state.fontSize = fontSize;
        this.notifyStateChange();
        this.eventHandlers?.onFontSizeChange(fontSize);
    }

    /**
     * Handle an auto approve change.
     * @param autoApprove The new auto approve value
     */
    handleAutoApproveChange(autoApprove: boolean): void {
        this.state.autoApprove = autoApprove;
        this.notifyStateChange();
        this.eventHandlers?.onAutoApproveChange(autoApprove);
    }

    /**
     * Handle a settings toggle.
     */
    handleSettingsToggle(): void {
        this.state.showSettings = !this.state.showSettings;
        this.notifyStateChange();
        this.eventHandlers?.onSettingsToggle();
    }

    /**
     * Handle a history toggle.
     */
    handleHistoryToggle(): void {
        this.state.showHistory = !this.state.showHistory;
        this.notifyStateChange();
        this.eventHandlers?.onHistoryToggle();
    }

    /**
     * Handle an MCP toggle.
     */
    handleMcpToggle(): void {
        this.state.showMcp = !this.state.showMcp;
        this.notifyStateChange();
        this.eventHandlers?.onMcpToggle();
    }

    /**
     * Handle an account toggle.
     */
    handleAccountToggle(): void {
        this.state.showAccount = !this.state.showAccount;
        this.notifyStateChange();
        this.eventHandlers?.onAccountToggle();
    }

    /**
     * Handle a task submit.
     * @param task The task to submit
     * @param options The task options
     */
    async handleTaskSubmit(task: string, options?: TaskOptions): Promise<TaskResult> {
        this.state.taskStatus = { status: 'running', message: 'Task is running', progress: 0 };
        this.notifyStateChange();
        this.eventHandlers?.onTaskSubmit(task, options);

        try {
            const result = await this.bridge.executeTask(task, options);
            this.state.taskStatus = { status: 'completed', message: 'Task completed', progress: 100 };
            this.notifyStateChange();
            return result;
        } catch (error) {
            this.state.taskStatus = { status: 'failed', message: `Task failed: ${error}` };
            this.notifyStateChange();
            throw error;
        }
    }

    /**
     * Handle a task cancel.
     */
    async handleTaskCancel(): Promise<void> {
        await this.bridge.cancelTask();
        this.state.taskStatus = { status: 'idle', message: 'Task cancelled' };
        this.notifyStateChange();
        this.eventHandlers?.onTaskCancel();
    }

    /**
     * Get the current UI state.
     */
    getState(): UIState {
        return { ...this.state };
    }

    /**
     * Notify the UI of a state change.
     */
    private notifyStateChange(): void {
        window.updateUI(this.state);
    }
}
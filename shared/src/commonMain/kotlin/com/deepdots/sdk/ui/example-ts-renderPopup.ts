import {DeepdotsEventType, PopupActions, FormData} from '../types';
import magicfeedback from "@magicfeedback/native";

// Injects the MagicFeedback stylesheet directly into the popup so styles work even when the bundler doesn't inject them globally.
function ensureMagicFeedbackStyles(popup: HTMLElement) {
    const DATA_ATTR = 'data-magicfeedback-css';
    // Skip if it already exists in the document head or inside the popup
    if (document.querySelector(`link[${DATA_ATTR}]`) || popup.querySelector(`link[${DATA_ATTR}]`)) {
        return;
    }
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://cdn.jsdelivr.net/npm/@magicfeedback/popup-sdk/dist/assets/assets/style.css';
    link.setAttribute(DATA_ATTR, 'true');
    // Insert at the top of the popup so specific styles load first
    popup.appendChild(link);
}

// Adds spinner styles if they aren't already present
function ensureSpinnerStyles(popup: HTMLElement) {
    if (document.getElementById('deepdots-spinner-styles')) return;
    const style = document.createElement('style');
    style.id = 'deepdots-spinner-styles';
    style.textContent = `
    @keyframes ddspin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
    .mf-spinner { display:flex; justify-content:center; align-items:center; padding:8px 0; }
    .mf-spinner-circle { width:28px; height:28px; border:3px solid #e0e6ed; border-top-color:#1E293B; border-radius:50%; animation: ddspin 0.9s linear infinite; }
  `;
    popup.appendChild(style);
}

function ensureResponsiveStyles(popup: HTMLElement) {
    if (document.getElementById('deepdots-responsive-styles')) return;
    const style = document.createElement('style');
    style.id = 'deepdots-responsive-styles';
    style.textContent = `
    /* Responsive adjustments */
    @media (max-width: 640px) {
      .deepdots-popup {
        width: 100% !important;
        max-width: 100% !important;
        height: 90vh !important;
        max-height: 100vh !important;
        border-radius: 0 !important;
        padding: calc(16px + env(safe-area-inset-top)) 16px calc(16px + env(safe-area-inset-bottom)) 16px !important;
        box-sizing: border-box;
      }
      .deepdots-popup .mf-spinner-circle { width: 32px; height: 32px; border-width: 4px; }
      .deepdots-popup button { font-size: 16px !important; }
      .deepdots-popup-header button { width:48px; height:48px; }
      .deepdots-popup-header button svg { width:26px; height:26px; }
      .deepdots-popup-footer { flex-direction: column-reverse !important; gap: 12px !important; }
      .deepdots-popup-footer button { width: 100%; }
    }
    @media (max-width: 400px) {
      .deepdots-popup { padding: calc(12px + env(safe-area-inset-top)) 12px calc(12px + env(safe-area-inset-bottom)) 12px !important; }
      .deepdots-popup-header button { width:48px; height:48px; }
      .deepdots-popup-header button svg { width:26px; height:26px; }
    }
    @media (orientation: landscape) and (max-height: 480px) {
      .deepdots-popup {
        height: 100vh !important;
        max-height: 100vh !important;
        overflow-y: auto !important;
      }
    }
  `;
    popup.appendChild(style);
}

/**
 * Renders the popup inside the given container using MagicFeedback for the survey.
 */
export async function renderPopup(
    container: HTMLElement,
    surveyId: string,
    productId: string,
    actions: PopupActions | undefined,
    emit: (type: DeepdotsEventType, surveyId: string, data?: Record<string, unknown>) => void,
    onClose: () => void
): Promise<void> {
    let surveyCompletedEmitted = false;
    let stylesInjected = false;
    // Build base popup element
    const popup = document.createElement('div');
    popup.className = 'deepdots-popup';
    popup.style.cssText = `
      position: relative;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      background: #fff;
      border-radius: 8px;
      padding: 24px;
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
      max-width: 600px;
      width: 90%;
      min-height: 200px;
    `;

    // Header section (close button only)
    const header = document.createElement('div');
    header.className = 'deepdots-popup-header';
    header.style.cssText = 'display:flex; justify-content:flex-end; align-items:center; width:100%;';

    // Close button (X)
    const closeBtn = document.createElement('button');
    closeBtn.type = 'button';
    closeBtn.setAttribute('aria-label', 'Close popup');
    closeBtn.innerHTML = `
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M6 6L18 18M6 18L18 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    `;
    closeBtn.style.cssText = `
      background:transparent;
      border:none;
      width:32px;
      height:32px;
      display:flex;
      align-items:center;
      justify-content:center;
      border-radius: 8px;
      cursor:pointer;
      color:#111;
      padding:4px;
      transition: color .15s ease, transform .15s ease, background .15s ease;
      box-shadow: none !important;
    `;
    closeBtn.onmouseenter = () => {
        closeBtn.style.color = '#000000';
        closeBtn.style.background = 'rgba(0,0,0,0.06)';
        closeBtn.style.transform = 'scale(1.06)';
    };
    closeBtn.onmouseleave = () => {
        closeBtn.style.color = '#111';
        closeBtn.style.background = 'transparent';
        closeBtn.style.transform = 'scale(1)';
    };
    closeBtn.onclick = () => {
        emit('popup_clicked', surveyId, {action: 'close_icon'});
        onClose();
    };
    header.appendChild(closeBtn);

    ensureMagicFeedbackStyles(popup);
    ensureSpinnerStyles(popup);
    ensureResponsiveStyles(popup);

    const containerContent = document.createElement('div');
    containerContent.className = 'deepdots-popup-container-conetent';
    containerContent.style.cssText = `
    display:flex; 
    flex-direction:column; 
    padding: 0 20px 12px 20px;
      max-height: 80vh; /* overall popup height limit */
      overflow: hidden; /* disable scrolling on the main container */
`

    // Main section: form + spinner container
    const main = document.createElement('div');
    main.className = 'deepdots-popup-main';
    main.style.cssText = 'display:flex; flex-direction:column; width:100%; max-height:80vh; overflow-y:auto;';

    const formWrapper = document.createElement('div');
    formWrapper.style.cssText = 'width:100%; flex: 1 1 auto;';

    // Validation error hint container
    const errorHint = document.createElement('div');
    errorHint.className = 'deepdots-error-hint';
    errorHint.style.cssText = `
      display: none;
      margin: 12px 0 0 0;
      padding: 10px 12px;
      border-radius: 6px;
      background: #FEF3C7; /* amber-100 */
      color: #92400E; /* amber-700 */
      border: 1px solid #FCD34D; /* amber-300 */
      font-size: 13px;
    `;
    errorHint.setAttribute('role', 'alert');
    errorHint.setAttribute('aria-live', 'polite');

    const spinnerEl = document.createElement('div');
    spinnerEl.className = 'mf-spinner';
    spinnerEl.setAttribute('role', 'status');
    spinnerEl.setAttribute('aria-label', 'Loading survey');
    spinnerEl.innerHTML = '<div class="mf-spinner-circle"></div>';
    spinnerEl.style.cssText = 'position:absolute; top:50%; left:50%; transform:translate(-50%,-50%);';

    const formDivId = `magicfeedback-form-${surveyId}`;
    const formHost = document.createElement('div');
    formHost.id = formDivId;
    formHost.style.cssText = 'width:100%; visibility:hidden;';

    formWrapper.appendChild(spinnerEl);
    formWrapper.appendChild(formHost);
    main.appendChild(formWrapper);
    // Insert the hint right above the footer
    main.appendChild(errorHint);

    // Footer section (actions) — buttons on either edge
    const footer = document.createElement('div');
    footer.className = 'deepdots-popup-footer';
    footer.setAttribute('data-actions-wrapper', 'true');
    footer.style.cssText = 'display:flex; flex-direction: row-reverse ;justify-content:space-between; align-items:center; gap:8px; margin-top:auto; width:100%; padding-top:16px;';

    // Buttons
    const backButton = document.createElement('button');
    backButton.textContent = actions?.back ? actions.back.label : 'Back';
    backButton.style.cssText = `
      background: transparent;
      color: #333;
      border: 1px solid #999;
      padding: 12px 24px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: filter .15s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-align: center;
    `;
    backButton.onmouseenter = () => {
        backButton.style.filter = 'brightness(0.9)';
    }
    backButton.onmouseleave = () => {
        backButton.style.filter = 'brightness(1)';
    }
    backButton.onclick = () => {
        emit('popup_clicked', surveyId, {action: 'back'});
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (formInstance as any)?.back?.();
    };

    // Start-survey button: only shown when the survey begins with a start message.
    // Rendered at 100% width so it fills the footer.
    const startButton = document.createElement('button');
    startButton.textContent = actions?.start ? actions.start.label : 'Start survey';
    startButton.style.cssText = `
      background: #1E293B;
      color: #fff;
      border: none;
      padding: 12px 24px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: filter .15s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-align: center;
    `;
    startButton.onclick = () => {
        emit('popup_clicked', surveyId, {action: 'start_survey'});
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (formInstance as any)?.startForm?.();
    };

    // Close-popup button: only shown once the survey is completed.
    // Rendered at 100% width so it fills the footer.
    const closeButton = document.createElement('button');
    closeButton.textContent = actions?.complete ? actions.complete.label : 'Complete survey';
    closeButton.style.cssText = `
      background: #1E293B;
      color: #fff;
      border: none;
      padding: 12px 24px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: filter .15s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-align: center;
    `;
    closeButton.onmouseenter = () => {
        closeButton.style.filter = 'brightness(0.9)';
    }
    closeButton.onmouseleave = () => {
        closeButton.style.filter = 'brightness(1)';
    }
    closeButton.onclick = () => {
        emit('popup_clicked', surveyId, {action: 'complete'});
        onClose();
    };

    // Send button: full width on the first page, otherwise pinned to the right side.
    const submitButton = document.createElement('button');
    submitButton.textContent = actions?.accept ? actions.accept.label : 'Send';
    submitButton.style.cssText = `
      background: #1E293B;
      color: #fff;
      border: none;
      padding: 12px 24px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: filter .15s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-align: center;
    `;
    submitButton.onclick = () => {
        if (!surveyCompletedEmitted) {
            emit('popup_clicked', surveyId, {action: 'manual_send'});
            // Trigger the native submit if available
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (formInstance as any)?.send?.();
        }
    };

    backButton.style.display = 'none';
    startButton.style.display = 'none';
    submitButton.style.display = 'none';
    closeButton.style.display = 'none';

    // Insert buttons into the footer in visual order (row-reverse keeps the primary on the right)
    footer.appendChild(submitButton);
    footer.appendChild(backButton);
    footer.appendChild(closeButton);
    footer.appendChild(startButton);

    // Attach footer to main and main to containerContent
    main.appendChild(footer);
    containerContent.appendChild(main);

    // Helper that toggles button visibility based on the current view state
    type ViewState = 'loading' | 'start' | 'in_progress_first' | 'in_progress_next' | 'completed' | 'error';

    function updateButtons(state: ViewState) {
        // Hide everything by default
        backButton.style.display = 'none';
        startButton.style.display = 'none';
        submitButton.style.display = 'none';
        closeButton.style.display = 'none';
        // Reset widths between states
        backButton.style.width = '';
        startButton.style.width = '';
        submitButton.style.width = '';
        closeButton.style.width = '';

        switch (state) {
            case 'loading':
                // Footer is hidden from setLoading
                break;
            case 'start':
                // Only the Start button, full width
                startButton.style.display = 'inline-flex';
                startButton.style.width = '100%';
                break;
            case 'in_progress_first':
                // Only the Send button (right side), auto width
                submitButton.style.display = 'inline-flex';
                submitButton.style.width = '';
                setLoading(false);
                break;
            case 'in_progress_next':
                // Show Back (left) + Send (right)
                backButton.style.display = 'inline-flex';
                submitButton.style.display = 'inline-flex';
                setLoading(false);
                break;
            case 'completed':
                // Show Close/Complete full width as the primary action
                closeButton.style.display = 'inline-flex';
                closeButton.style.width = '100%';
                setLoading(false);
                break;
            case 'error':
                // On error, allow closing (auto width)
                // closeButton.style.display = 'inline-flex';
                setLoading(false);
                break;
        }
    }

    // Assemble popup
    popup.appendChild(header);
    popup.appendChild(containerContent);

    container.innerHTML = '';
    container.appendChild(popup);
    container.style.display = 'flex';

    // Dynamic loading state management
    function setLoading(isLoading: boolean) {
        spinnerEl.style.display = isLoading ? 'flex' : 'none';
        if (!isLoading) {
            formHost.style.visibility = 'visible';
        }
        // Fully hide the footer buttons while loading
        footer.style.display = isLoading ? 'none' : 'flex';
        // Disable buttons in case they are visible
        backButton.disabled = isLoading;
        startButton.disabled = isLoading;
        closeButton.disabled = isLoading;
        submitButton.disabled = isLoading;
        submitButton.style.opacity = isLoading ? '0.6' : '1';
        submitButton.style.cursor = isLoading ? 'not-allowed' : 'pointer';
        // Don't overwrite the button state when loading finishes.
        if (isLoading) {
            updateButtons('loading');
        }
    }

    // Initial state
    setLoading(true);

    // Browser environment guard
    if (typeof window === 'undefined' || typeof document === 'undefined') {
        return;
    }

    // Reference to the form instance
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let formInstance: any = null;

    try {
        if (!magicfeedback || typeof magicfeedback.form !== 'function') {
            console.warn('[MagicFeedback] form() not available. Falling back to manual rendering.');
            setLoading(false);
            return;
        }
        magicfeedback.init({debug: true, env: 'prod'});
        formInstance = magicfeedback.form(surveyId, productId);

        interface TypedGenerateOptions {
            addButton: boolean;
            getMetaData: boolean;
            onLoadedEvent?: (args: {
                formData: FormData,
                progress?: number, total?: number
            }) => void;
            beforeSubmitEvent?: () => void;
            afterSubmitEvent?: (args: { error?: string, completed: boolean, progress: number, total: number }) => void;
            onBackEvent?: (args: { error?: string, progress: number, total: number, followup: boolean }) => void;
        }

        const generateOptions: TypedGenerateOptions = {
            addButton: false,
            getMetaData: true,
        };
        generateOptions.onLoadedEvent = ({formData}) => {
            // Compute available height and apply it to main (subtract header + footer + paddings)
            try {
                /*
                const headerHeight = header.getBoundingClientRect().height;
                const footerHeight = footer.getBoundingClientRect().height;
                const paddingY = 48; // 24px top + 24px bottom
                const viewportLimit = window.innerHeight * 0.8; // matches the popup max-height
                const available = viewportLimit - headerHeight - footerHeight - paddingY;
                if (available > 120) { // keep a sensible minimum
                    main.style.maxHeight = available + 'px';
                } */
            } catch (e) {
                // swallowed
            }
            // Apply popup customization driven by formData.style
            const s = formData?.style;
            if (s && !stylesInjected) {
                stylesInjected = true;
                // Popup container background
                if (s.boxBackgroundColor) {
                    popup.style.background = s.boxBackgroundColor;
                }
                // Main content alignment
                if (s.contentAlign) {
                    // 'top' => start, 'center' => center
                    main.style.justifyContent = s.contentAlign === 'center' ? 'center' : 'flex-start';
                }
                // Primary button (submit, start)
                if (s.buttonPrimaryColor) {
                    submitButton.style.background = s.buttonPrimaryColor;
                    submitButton.style.border = 'none';
                    submitButton.style.color = '#fff';

                    startButton.style.background = s.buttonPrimaryColor;
                    startButton.style.border = 'none';
                    startButton.style.color = '#fff';

                    closeButton.style.background = s.buttonPrimaryColor;
                    closeButton.style.border = 'none';
                    closeButton.style.color = '#fff';
                }
                // Secondary button (back)
                if (s.buttonSecondaryColor) {

                    backButton.style.color = '#fff';
                    backButton.style.border = `1px solid ${s.buttonSecondaryColor}`;
                }
                if (s.logo) {
                    if (!document.getElementById('deepdots-popup-logo')) {
                        // Insert logo if provided
                        const logoImg = document.createElement('img');
                        logoImg.id = 'deepdots-popup-logo';
                        logoImg.src = s.logo;
                        logoImg.alt = 'Logo';
                        logoImg.style.cssText = 'max-height:40px; max-width:100%; object-fit:contain;';
                        if (s.logoSize) {
                            switch (s.logoSize) {
                                case 'small':
                                    logoImg.style.maxHeight = '30px';
                                    break;
                                case 'medium':
                                    logoImg.style.maxHeight = '50px';
                                    break;
                                case 'large':
                                    logoImg.style.maxHeight = '70px';
                                    break;
                            }
                        }
                        if (s.logoPosition) {
                            switch (s.logoPosition) {
                                case 'left':
                                    logoImg.style.margin = '0 16px 42px 0';
                                    logoImg.style.display = 'block';
                                    logoImg.style.marginLeft = '0';
                                    break;
                                case 'right':
                                    logoImg.style.margin = '0 0 42px 16px';
                                    logoImg.style.display = 'block';
                                    logoImg.style.marginLeft = 'auto';
                                    break;
                                case 'center':
                                    logoImg.style.margin = '0 auto 42px auto';
                                    logoImg.style.display = 'block';
                                    break;
                            }
                        }
                        // Insert above main if not already present
                        containerContent.insertBefore(logoImg, main);
                    }
                }

                if (s.startMessage && s.startMessage !== '') {
                    // With a start message, show the Start button first
                    console.log(s.startMessage);
                    updateButtons('start');
                } else {
                    // No start message → render the first-page state (Send only)
                    updateButtons('in_progress_first');
                }
            } else {
                // No styles provided, assume a normal first page
                updateButtons('in_progress_first');
            }

            emit('popup_clicked', surveyId, {action: 'loaded'});
            setLoading(false); // reveals the form and hides the spinner
        };
        generateOptions.beforeSubmitEvent = () => {
            setLoading(true);
            emit('popup_clicked', surveyId, {action: 'before_submit'});
        };
        generateOptions.afterSubmitEvent = ({error, completed, total, progress}) => {
            // Don't change the loading state here; each transition handles it
            // Normalize the error to safe text
            const errText = error ? (typeof error === 'string' ? error : ((error as unknown as {message?: string}).message ?? String(error))) : '';
            if (errText) {
                // Turn off loading so buttons are visible again
                setLoading(false);
                // Special case: required-question validation error
                if (errText.toLowerCase().includes('no response')) {
                    errorHint.textContent = 'Please answer the required question to continue.';
                    errorHint.style.display = 'block';
                    emit('popup_clicked', surveyId, {action: 'validation_error_required'});
                    updateButtons('in_progress_next');
                    return;
                }
                // Other errors: show a generic message and let the user close
                errorHint.textContent = 'An error occurred while submitting. Please try again or close the popup.';
                errorHint.style.display = 'block';
                emit('popup_clicked', surveyId, {action: 'submit_error', error: errText});
                // updateButtons('error');
                return;
            }
            // Clear the hint when there's no error
            errorHint.style.display = 'none';
            setLoading(false);
            if (completed) {
                emit('survey_completed', surveyId, {action: 'completed'});
                surveyCompletedEmitted = true;
                updateButtons('completed');
                return;
            }
            if (total > 1 && progress > 0 && progress < total) {
                updateButtons('in_progress_next');
            } else {
                updateButtons('in_progress_first');
            }
        };
        generateOptions.onBackEvent = ({progress}) => {
            emit('popup_clicked', surveyId, {action: 'back'});
            // Hide Back when returning to the first page
            if (progress === 0) {
                updateButtons('in_progress_first');
            } else {
                updateButtons('in_progress_next');
            }
        };

        // Run form generation with the typed options
        formInstance.generate(formDivId, generateOptions)
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            .catch((err: any) => {
                console.error('[MagicFeedback] Error generating form:', err);
                setLoading(false);
            });
    } catch (e) {
        console.error('[MagicFeedback] Exception initializing form:', e);
        setLoading(false);
    }
}
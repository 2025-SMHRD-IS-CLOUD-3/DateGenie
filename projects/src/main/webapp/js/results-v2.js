/**
 * Results V2 Page - Enhanced Dating Analysis Results
 * Mobile-first design with smooth animations and interactions
 * 
 * Features:
 * - Circular progress chart animation
 * - Emotion bars with staggered animations
 * - Responsive design for all screen sizes
 * - Dark mode support
 * - Error handling and loading states
 * - Accessibility support
 */

class ResultsV2Manager {
    constructor() {
        this.isAnimationComplete = false;
        this.animationQueue = [];
        this.currentData = null;
        this.observers = [];
        
        this.init();
    }

    /**
     * Initialize the results page
     */
    init() {
        this.setupIntersectionObserver();
        this.loadAnalysisData();
        this.initializeAnimations();
        this.setupAccessibility();
        this.handleErrors();
    }

    /**
     * Set up intersection observer for scroll-triggered animations
     */
    setupIntersectionObserver() {
        if ('IntersectionObserver' in window) {
            const observerOptions = {
                threshold: 0.3,
                rootMargin: '0px 0px -50px 0px'
            };

            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        this.triggerCardAnimation(entry.target);
                    }
                });
            }, observerOptions);

            // Observe all result cards
            document.querySelectorAll('.result-card').forEach(card => {
                observer.observe(card);
            });

            this.observers.push(observer);
        }
    }

    /**
     * Load analysis data (from API or localStorage)
     */
    async loadAnalysisData() {
        try {
            // Try to load from localStorage first
            const storedData = localStorage.getItem('analysisResults');
            
            if (storedData) {
                this.currentData = JSON.parse(storedData);
            } else {
                // Use mock data as fallback
                this.currentData = this.getMockAnalysisData();
            }

            this.displayResults(this.currentData);
        } catch (error) {
            console.error('Error loading analysis data:', error);
            this.showErrorState();
        }
    }

    /**
     * Get mock analysis data for testing
     */
    getMockAnalysisData() {
        return {
            datingPossibility: Math.floor(Math.random() * 40) + 40, // 40-80%
            emotions: [
                { name: "호감 표현", value: Math.floor(Math.random() * 30) + 60 },
                { name: "관심 표시", value: Math.floor(Math.random() * 25) + 55 },
                { name: "친밀감", value: Math.floor(Math.random() * 25) + 50 },
                { name: "유머 반응", value: Math.floor(Math.random() * 20) + 45 },
                { name: "미래 계획", value: Math.floor(Math.random() * 20) + 35 }
            ],
            favoriteSentences: [
                {
                    text: "너와 이야기하면 시간이 어떻게 가는지 모르겠어",
                    source: "대화 중 자주 나타나는 표현"
                },
                {
                    text: "다음에는 우리 함께 가볼까?",
                    source: "미래 계획에 대한 긍정적 언급"
                },
                {
                    text: "너 정말 재밌는 사람이야",
                    source: "상대방에 대한 직접적 호감 표현"
                }
            ],
            personality: ["외향적", "유머러스", "적극적", "배려심 많음", "솔직함", "계획적"],
            advice: [
                {
                    type: "positive",
                    title: "지금이 기회입니다",
                    content: "상대방이 당신에게 충분한 관심을 보이고 있습니다. 직접적인 만남을 제안해보는 것이 좋겠어요."
                },
                {
                    type: "info",
                    title: "대화 스타일 유지",
                    content: "현재의 유머러스하고 자연스러운 대화 스타일을 계속 유지하세요. 상대방이 이런 분위기를 좋아합니다."
                },
                {
                    type: "warning",
                    title: "적절한 타이밍",
                    content: "너무 급하게 관계를 발전시키려 하지 마세요. 자연스러운 흐름을 따라가는 것이 중요합니다."
                }
            ],
            generatedAt: new Date().toISOString()
        };
    }

    /**
     * Display analysis results in the UI
     */
    displayResults(data) {
        try {
            // Update dating possibility
            this.updateDatingPossibility(data.datingPossibility);
            
            // Update emotion bars
            this.updateEmotionBars(data.emotions);
            
            // Update other sections
            this.updateFavoriteSentences(data.favoriteSentences);
            this.updatePersonality(data.personality);
            this.updateAdvice(data.advice);
            
            // Store data for sharing
            this.currentData = data;
            
        } catch (error) {
            console.error('Error displaying results:', error);
            this.showErrorState();
        }
    }

    /**
     * Update dating possibility section
     */
    updateDatingPossibility(percentage) {
        const textElement = document.getElementById('progressText');
        const descriptionElement = document.querySelector('.possibility-description');
        
        if (textElement) {
            textElement.textContent = percentage + '%';
        }
        
        if (descriptionElement) {
            const description = this.generatePossibilityDescription(percentage);
            descriptionElement.textContent = description;
        }
    }

    /**
     * Generate description based on possibility percentage
     */
    generatePossibilityDescription(percentage) {
        if (percentage >= 70) {
            return "분석 결과, 상대방이 당신에게 호감을 가지고 있을 가능성이 매우 높습니다. 적극적으로 다가가세요!";
        } else if (percentage >= 50) {
            return "분석 결과, 상대방이 당신에게 호감을 가지고 있을 가능성이 높습니다. 긍정적인 신호들이 많이 발견되었네요!";
        } else if (percentage >= 30) {
            return "분석 결과, 상대방이 당신에게 관심을 보이고 있습니다. 조금 더 시간을 두고 관계를 발전시켜보세요.";
        } else {
            return "분석 결과, 아직 명확한 신호를 파악하기 어렵습니다. 더 많은 대화가 필요할 것 같아요.";
        }
    }

    /**
     * Update emotion bars with data
     */
    updateEmotionBars(emotions) {
        const emotionBars = document.getElementById('emotionBars');
        if (!emotionBars || !emotions) return;

        emotionBars.innerHTML = '';
        
        emotions.forEach((emotion, index) => {
            const barElement = this.createEmotionBar(emotion.name, emotion.value);
            emotionBars.appendChild(barElement);
        });
    }

    /**
     * Create emotion bar element
     */
    createEmotionBar(name, value) {
        const barContainer = document.createElement('div');
        barContainer.className = 'emotion-bar';
        
        barContainer.innerHTML = `
            <div class="emotion-label">
                <span class="emotion-name">${name}</span>
                <span class="emotion-value">${value}</span>
            </div>
            <div class="emotion-progress">
                <div class="emotion-progress-fill" data-width="${value}"></div>
            </div>
        `;
        
        return barContainer;
    }

    /**
     * Update favorite sentences section
     */
    updateFavoriteSentences(sentences) {
        const container = document.querySelector('.sentences-list');
        if (!container || !sentences) return;

        container.innerHTML = '';
        
        sentences.forEach(sentence => {
            const sentenceElement = document.createElement('div');
            sentenceElement.className = 'sentence-item';
            sentenceElement.innerHTML = `
                "${sentence.text}"
                <div class="sentence-source">${sentence.source}</div>
            `;
            container.appendChild(sentenceElement);
        });
    }

    /**
     * Update personality traits section
     */
    updatePersonality(traits) {
        const container = document.querySelector('.personality-traits');
        if (!container || !traits) return;

        container.innerHTML = '';
        
        traits.forEach(trait => {
            const traitElement = document.createElement('div');
            traitElement.className = 'trait-tag';
            traitElement.textContent = trait;
            container.appendChild(traitElement);
        });
    }

    /**
     * Update advice section
     */
    updateAdvice(adviceList) {
        const container = document.querySelector('.advice-list');
        if (!container || !adviceList) return;

        container.innerHTML = '';
        
        adviceList.forEach(advice => {
            const adviceElement = document.createElement('div');
            adviceElement.className = `advice-item ${advice.type}`;
            
            const iconClass = this.getAdviceIcon(advice.type);
            
            adviceElement.innerHTML = `
                <div class="advice-title">
                    <i class="${iconClass}"></i>
                    ${advice.title}
                </div>
                <p class="advice-content">${advice.content}</p>
            `;
            
            container.appendChild(adviceElement);
        });
    }

    /**
     * Get appropriate icon for advice type
     */
    getAdviceIcon(type) {
        const icons = {
            positive: 'fas fa-thumbs-up',
            info: 'fas fa-heart',
            warning: 'fas fa-clock',
            default: 'fas fa-lightbulb'
        };
        
        return icons[type] || icons.default;
    }

    /**
     * Initialize animations
     */
    initializeAnimations() {
        // Start main animations after page load
        setTimeout(() => {
            this.animateCircularProgress();
        }, 1000);
        
        setTimeout(() => {
            this.animateEmotionBars();
        }, 1500);
    }

    /**
     * Animate circular progress chart
     */
    animateCircularProgress() {
        const circle = document.getElementById('progressCircle');
        const text = document.getElementById('progressText');
        
        if (!circle || !text) return;

        const percentage = this.currentData?.datingPossibility || 50;
        const circumference = 2 * Math.PI * 90; // r=90
        const offset = circumference - (percentage / 100) * circumference;
        
        // Set initial state
        circle.style.strokeDasharray = circumference;
        circle.style.strokeDashoffset = circumference;
        
        // Animate progress
        setTimeout(() => {
            circle.style.strokeDashoffset = offset;
        }, 100);

        // Animate text counter
        this.animateCounter(text, 0, percentage, 2000, '%');
    }

    /**
     * Animate emotion bars
     */
    animateEmotionBars() {
        const bars = document.querySelectorAll('.emotion-progress-fill');
        
        bars.forEach((bar, index) => {
            const width = bar.getAttribute('data-width');
            setTimeout(() => {
                bar.style.width = width + '%';
            }, index * 200);
        });
    }

    /**
     * Animate counter with easing
     */
    animateCounter(element, start, end, duration, suffix = '') {
        let startTime = null;
        
        const animate = (currentTime) => {
            if (!startTime) startTime = currentTime;
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            // Ease out cubic
            const easedProgress = 1 - Math.pow(1 - progress, 3);
            const currentValue = Math.round(start + (end - start) * easedProgress);
            
            element.textContent = currentValue + suffix;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    }

    /**
     * Trigger card-specific animations on scroll
     */
    triggerCardAnimation(card) {
        card.style.animationDelay = '0s';
        card.classList.add('animate-in');
    }

    /**
     * Setup accessibility features
     */
    setupAccessibility() {
        // Add screen reader announcements for animations
        const srAnnouncer = document.createElement('div');
        srAnnouncer.setAttribute('aria-live', 'polite');
        srAnnouncer.setAttribute('aria-atomic', 'true');
        srAnnouncer.className = 'sr-only';
        document.body.appendChild(srAnnouncer);
        
        this.srAnnouncer = srAnnouncer;
        
        // Announce when animations complete
        setTimeout(() => {
            this.announceResults();
        }, 3000);
    }

    /**
     * Announce results for screen readers
     */
    announceResults() {
        if (!this.currentData || !this.srAnnouncer) return;
        
        const announcement = `분석 완료. 연애 가능성 ${this.currentData.datingPossibility}퍼센트입니다.`;
        this.srAnnouncer.textContent = announcement;
    }

    /**
     * Handle errors and show appropriate fallbacks
     */
    handleErrors() {
        window.addEventListener('error', (event) => {
            console.error('Results page error:', event.error);
            this.showErrorState();
        });
        
        // Handle unhandled promise rejections
        window.addEventListener('unhandledrejection', (event) => {
            console.error('Unhandled promise rejection:', event.reason);
            this.showErrorState();
        });
    }

    /**
     * Show error state when something goes wrong
     */
    showErrorState() {
        const mainContainer = document.querySelector('.results-v2-container');
        if (!mainContainer) return;
        
        mainContainer.innerHTML = `
            <div class="result-card" style="text-align: center; padding: 3rem;">
                <i class="fas fa-exclamation-triangle" style="font-size: 3rem; color: #f59e0b; margin-bottom: 1rem;"></i>
                <h2 style="color: #1f1d3a; margin-bottom: 1rem;">분석 결과를 불러올 수 없습니다</h2>
                <p style="color: #6b7280; margin-bottom: 2rem;">
                    일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.
                </p>
                <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                    <button onclick="window.location.reload()" class="btn-primary-v2">
                        <i class="fas fa-redo"></i>
                        다시 시도
                    </button>
                    <a href="upload.html" class="btn-ghost-v2">
                        <i class="fas fa-upload"></i>
                        새 분석 시작
                    </a>
                </div>
            </div>
        `;
    }

    /**
     * Export analysis results for sharing
     */
    exportResults() {
        if (!this.currentData) return null;
        
        return {
            ...this.currentData,
            exportedAt: new Date().toISOString(),
            pageType: 'results-v2'
        };
    }

    /**
     * Clean up observers and event listeners
     */
    destroy() {
        this.observers.forEach(observer => observer.disconnect());
        this.observers = [];
        
        if (this.srAnnouncer) {
            this.srAnnouncer.remove();
        }
    }
}

// Initialize the results manager when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.resultsV2Manager = new ResultsV2Manager();
});

// Export for potential module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ResultsV2Manager;
}
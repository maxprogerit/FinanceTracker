// API Base URL
const API_BASE = '/api';

// Global state
let currentUser = null;
let authToken = null;
let categories = [];
let expenses = [];
let budgets = [];

// DOM Elements
const authContainer = document.getElementById('auth-container');
const dashboard = document.getElementById('dashboard');
const loginForm = document.getElementById('login-form');
const signupForm = document.getElementById('signup-form');
const userInfo = document.getElementById('user-info');
const usernameDisplay = document.getElementById('username-display');

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    // Check for saved token
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    if (savedToken && savedUser) {
        authToken = savedToken;
        currentUser = JSON.parse(savedUser);
        showDashboard();
    }
    
    initializeEventListeners();
});

// Event Listeners
function initializeEventListeners() {
    // Auth form toggles
    document.getElementById('show-signup').addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.add('hidden');
        signupForm.classList.remove('hidden');
    });
    
    document.getElementById('show-login').addEventListener('click', (e) => {
        e.preventDefault();
        signupForm.classList.add('hidden');
        loginForm.classList.remove('hidden');
    });
    
    // Form submissions
    document.getElementById('login-form-element').addEventListener('submit', handleLogin);
    document.getElementById('signup-form-element').addEventListener('submit', handleSignup);
    document.getElementById('expense-form').addEventListener('submit', handleAddExpense);
    document.getElementById('budget-form').addEventListener('submit', handleAddBudget);
    
    // Logout
    document.getElementById('logout-btn').addEventListener('click', handleLogout);
    
    // Modal controls
    document.getElementById('add-expense-btn').addEventListener('click', () => {
        loadCategories().then(() => {
            document.getElementById('expense-modal').classList.remove('hidden');
        });
    });
    
    document.getElementById('add-budget-btn').addEventListener('click', () => {
        loadCategories().then(() => {
            document.getElementById('budget-modal').classList.remove('hidden');
        });
    });
    
    document.getElementById('close-expense-modal').addEventListener('click', () => {
        document.getElementById('expense-modal').classList.add('hidden');
    });
    
    document.getElementById('close-budget-modal').addEventListener('click', () => {
        document.getElementById('budget-modal').classList.add('hidden');
    });
    
    // Tab switching
    document.getElementById('tab-expenses').addEventListener('click', () => switchTab('expenses'));
    document.getElementById('tab-budgets').addEventListener('click', () => switchTab('budgets'));
    document.getElementById('tab-analytics').addEventListener('click', () => switchTab('analytics'));
}

// Authentication functions
async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const response = await fetch(`${API_BASE}/auth/signin`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                usernameOrEmail: username,
                password: password
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            authToken = data.token;
            currentUser = {
                id: data.id,
                username: data.username,
                email: data.email,
                firstName: data.firstName,
                lastName: data.lastName
            };
            
            // Save to localStorage
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showDashboard();
        } else {
            const error = await response.json();
            alert(error.message || 'Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed. Please try again.');
    }
}

async function handleSignup(e) {
    e.preventDefault();
    
    const formData = {
        username: document.getElementById('signup-username').value,
        email: document.getElementById('signup-email').value,
        firstName: document.getElementById('signup-firstname').value,
        lastName: document.getElementById('signup-lastname').value,
        password: document.getElementById('signup-password').value
    };
    
    try {
        const response = await fetch(`${API_BASE}/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (response.ok) {
            alert('Account created successfully! Please sign in.');
            signupForm.classList.add('hidden');
            loginForm.classList.remove('hidden');
        } else {
            const error = await response.json();
            alert(error.message || 'Signup failed');
        }
    } catch (error) {
        console.error('Signup error:', error);
        alert('Signup failed. Please try again.');
    }
}

function handleLogout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    
    dashboard.classList.add('hidden');
    authContainer.classList.remove('hidden');
    userInfo.classList.add('hidden');
}

// Dashboard functions
async function showDashboard() {
    authContainer.classList.add('hidden');
    dashboard.classList.remove('hidden');
    userInfo.classList.remove('hidden');
    
    usernameDisplay.textContent = currentUser.firstName || currentUser.username;
    
    await loadInitialData();
}

async function loadInitialData() {
    try {
        await Promise.all([
            loadCategories(),
            loadExpenses(),
            loadBudgets()
        ]);
        
        updateDashboard();
    } catch (error) {
        console.error('Error loading initial data:', error);
    }
}

// API functions
async function apiRequest(endpoint, options = {}) {
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };
    
    if (authToken) {
        config.headers.Authorization = `Bearer ${authToken}`;
    }
    
    const response = await fetch(`${API_BASE}${endpoint}`, config);
    
    if (response.status === 401) {
        handleLogout();
        throw new Error('Unauthorized');
    }
    
    return response;
}

async function loadCategories() {
    try {
        const response = await apiRequest('/categories');
        if (response.ok) {
            categories = await response.json();
            populateCategorySelects();
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

async function loadExpenses() {
    try {
        const response = await apiRequest('/expenses');
        if (response.ok) {
            expenses = await response.json();
            displayExpenses();
        }
    } catch (error) {
        console.error('Error loading expenses:', error);
    }
}

async function loadBudgets() {
    try {
        const response = await apiRequest('/budgets');
        if (response.ok) {
            budgets = await response.json();
            displayBudgets();
        }
    } catch (error) {
        console.error('Error loading budgets:', error);
    }
}

// UI Update functions
function populateCategorySelects() {
    const expenseSelect = document.getElementById('expense-category');
    const budgetSelect = document.getElementById('budget-category');
    
    expenseSelect.innerHTML = '<option value="">Select Category</option>';
    budgetSelect.innerHTML = '<option value="">Select Category</option>';
    
    categories.forEach(category => {
        const option1 = new Option(category.name, category.id);
        const option2 = new Option(category.name, category.id);
        expenseSelect.appendChild(option1);
        budgetSelect.appendChild(option2);
    });
}

function displayExpenses() {
    const container = document.getElementById('expenses-list');
    
    if (expenses.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-400">
                <i class="fas fa-receipt text-4xl mb-4 opacity-50"></i>
                <p>No expenses yet. Add your first expense!</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = expenses.map(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        const date = new Date(expense.expenseDate).toLocaleDateString();
        
        return `
            <div class="transaction-item bg-gray-700 p-4 rounded-lg flex items-center justify-between">
                <div class="flex items-center space-x-4">
                    <div class="w-12 h-12 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                        <span class="text-xl">${category?.iconName || '💰'}</span>
                    </div>
                    <div>
                        <h4 class="font-medium text-white">${expense.description}</h4>
                        <p class="text-sm text-gray-400">${category?.name || 'Unknown'} • ${date}</p>
                    </div>
                </div>
                <div class="text-right">
                    <p class="font-bold text-red-400">-$${expense.amount.toFixed(2)}</p>
                    <button class="text-xs text-gray-500 hover:text-gray-300" onclick="deleteExpense(${expense.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function displayBudgets() {
    const container = document.getElementById('budgets-list');
    
    if (budgets.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-400">
                <i class="fas fa-chart-pie text-4xl mb-4 opacity-50"></i>
                <p>No budgets set. Create your first budget!</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = budgets.map(budget => {
        const percentage = budget.percentageSpent;
        const isNearLimit = percentage >= budget.notificationThreshold;
        const isExceeded = budget.isExceeded;
        
        let statusColor = 'bg-green-500';
        if (isExceeded) statusColor = 'bg-red-500';
        else if (isNearLimit) statusColor = 'bg-yellow-500';
        
        return `
            <div class="bg-gray-700 p-4 rounded-lg">
                <div class="flex justify-between items-start mb-3">
                    <div>
                        <h4 class="font-medium text-white">${budget.name}</h4>
                        <p class="text-sm text-gray-400">${budget.categoryName}</p>
                    </div>
                    <div class="text-right">
                        <p class="text-white font-bold">$${budget.spentAmount.toFixed(2)} / $${budget.budgetLimit.toFixed(2)}</p>
                        <p class="text-sm ${isExceeded ? 'text-red-400' : 'text-gray-400'}">${percentage.toFixed(1)}% used</p>
                    </div>
                </div>
                <div class="w-full bg-gray-600 rounded-full h-2">
                    <div class="${statusColor} h-2 rounded-full transition-all duration-300" style="width: ${Math.min(percentage, 100)}%"></div>
                </div>
                ${isNearLimit ? `
                    <div class="mt-3 p-2 bg-yellow-500 bg-opacity-20 rounded text-yellow-300 text-sm">
                        <i class="fas fa-exclamation-triangle mr-1"></i>
                        ${isExceeded ? 'Budget exceeded!' : 'Approaching budget limit'}
                    </div>
                ` : ''}
            </div>
        `;
    }).join('');
}

function updateDashboard() {
    // Calculate total balance (this would typically come from accounts)
    const totalSpent = expenses.reduce((sum, expense) => sum + expense.amount, 0);
    document.getElementById('total-balance').textContent = `-$${totalSpent.toFixed(2)}`;
    
    // Update analytics if tab is active
    const analyticsTab = document.getElementById('analytics-tab');
    if (!analyticsTab.classList.contains('hidden')) {
        updateAnalytics();
    }
}

// Expense functions
async function handleAddExpense(e) {
    e.preventDefault();
    
    const formData = {
        amount: parseFloat(document.getElementById('expense-amount').value),
        description: document.getElementById('expense-description').value,
        categoryId: parseInt(document.getElementById('expense-category').value),
        notes: document.getElementById('expense-notes').value,
        expenseDate: new Date().toISOString()
    };
    
    try {
        const response = await apiRequest('/expenses', {
            method: 'POST',
            body: JSON.stringify(formData)
        });
        
        if (response.ok) {
            document.getElementById('expense-modal').classList.add('hidden');
            document.getElementById('expense-form').reset();
            await loadExpenses();
            await loadBudgets(); // Reload budgets to update spending
            updateDashboard();
        } else {
            alert('Failed to add expense');
        }
    } catch (error) {
        console.error('Error adding expense:', error);
        alert('Failed to add expense');
    }
}

async function deleteExpense(expenseId) {
    if (!confirm('Are you sure you want to delete this expense?')) return;
    
    try {
        const response = await apiRequest(`/expenses/${expenseId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            await loadExpenses();
            await loadBudgets();
            updateDashboard();
        } else {
            alert('Failed to delete expense');
        }
    } catch (error) {
        console.error('Error deleting expense:', error);
        alert('Failed to delete expense');
    }
}

// Budget functions
async function handleAddBudget(e) {
    e.preventDefault();
    
    const formData = {
        name: document.getElementById('budget-name').value,
        budgetLimit: parseFloat(document.getElementById('budget-limit').value),
        categoryId: parseInt(document.getElementById('budget-category').value),
        notificationThreshold: parseInt(document.getElementById('budget-threshold').value),
        periodType: 'MONTHLY'
    };
    
    try {
        const response = await apiRequest('/budgets', {
            method: 'POST',
            body: JSON.stringify(formData)
        });
        
        if (response.ok) {
            document.getElementById('budget-modal').classList.add('hidden');
            document.getElementById('budget-form').reset();
            await loadBudgets();
        } else {
            const error = await response.json();
            alert(error.message || 'Failed to create budget');
        }
    } catch (error) {
        console.error('Error creating budget:', error);
        alert('Failed to create budget');
    }
}

// Tab functions
function switchTab(tab) {
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('bg-blue-600');
        btn.classList.add('bg-gray-700', 'hover:bg-gray-600');
    });
    
    document.getElementById(`tab-${tab}`).classList.remove('bg-gray-700', 'hover:bg-gray-600');
    document.getElementById(`tab-${tab}`).classList.add('bg-blue-600');
    
    // Update content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.add('hidden');
    });
    
    document.getElementById(`${tab}-tab`).classList.remove('hidden');
    
    // Load specific tab data
    if (tab === 'analytics') {
        updateAnalytics();
    }
}

// Analytics functions
function updateAnalytics() {
    updateCategoryChart();
    updateTrendChart();
}

function updateCategoryChart() {
    const ctx = document.getElementById('category-chart');
    if (!ctx) return;
    
    // Calculate spending by category
    const categorySpending = {};
    expenses.forEach(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        const categoryName = category?.name || 'Unknown';
        categorySpending[categoryName] = (categorySpending[categoryName] || 0) + expense.amount;
    });
    
    const labels = Object.keys(categorySpending);
    const data = Object.values(categorySpending);
    const colors = labels.map((_, i) => `hsl(${i * 360 / labels.length}, 70%, 60%)`);
    
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: '#1f2937'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    labels: {
                        color: 'white'
                    }
                }
            }
        }
    });
}

function updateTrendChart() {
    const ctx = document.getElementById('trend-chart');
    if (!ctx) return;
    
    // Calculate monthly spending
    const monthlySpending = {};
    expenses.forEach(expense => {
        const date = new Date(expense.expenseDate);
        const monthKey = `${date.getFullYear()}-${date.getMonth() + 1}`;
        monthlySpending[monthKey] = (monthlySpending[monthKey] || 0) + expense.amount;
    });
    
    const labels = Object.keys(monthlySpending).sort();
    const data = labels.map(label => monthlySpending[label]);
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels.map(label => {
                const [year, month] = label.split('-');
                return new Date(year, month - 1).toLocaleDateString('en-US', { 
                    year: 'numeric', 
                    month: 'short' 
                });
            }),
            datasets: [{
                label: 'Monthly Spending',
                data: data,
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        color: 'white'
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                },
                x: {
                    ticks: {
                        color: 'white'
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                }
            },
            plugins: {
                legend: {
                    labels: {
                        color: 'white'
                    }
                }
            }
        }
    });
}
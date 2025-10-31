// API Base URL
const API_BASE = '/api';

// Global state
let currentUser = null;
let authToken = null;
let categories = [];
let expenses = [];
let budgets = [];
let incomes = [];
let incomeCategories = [];
let monthlyBudget = {
    totalIncome: 0,
    essentials: { allocated: 0, spent: 0, percentage: 50 }, // Rent, utilities, groceries, etc.
    growth: { allocated: 0, spent: 0, percentage: 25 },     // Investments
    emergency: { allocated: 0, spent: 0, percentage: 15 },  // Emergency fund/savings
    leisure: { allocated: 0, spent: 0, percentage: 10 }     // Entertainment, dining out, etc.
};

// Budget allocation settings - map expense categories to budget types
const budgetCategoryMapping = {
    essentials: ['Food & Dining', 'Bills & Utilities', 'Groceries', 'Gas & Fuel', 'Healthcare', 'Transport'],
    growth: ['Investment'],
    emergency: [], // Usually no direct expenses, just savings
    leisure: ['Entertainment', 'Shopping', 'Gifts & Donations']
};

// DOM Elements
const authContainer = document.getElementById('auth-container');
const dashboard = document.getElementById('dashboard');
const loginForm = document.getElementById('login-form');
const signupForm = document.getElementById('signup-form');
const userInfo = document.getElementById('user-info');
const usernameDisplay = document.getElementById('username-display');

// Initialize app
document.addEventListener('DOMContentLoaded', function () {
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
    document.getElementById('income-form').addEventListener('submit', handleAddIncome);
    document.getElementById('income-category-form').addEventListener('submit', handleAddIncomeCategory);

    // Logout
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    // Modal controls
    document.getElementById('add-expense-btn').addEventListener('click', async () => {
        try {
            await loadCategories();
            if (categories.length === 0) {
                alert('No expense categories available. Please create some categories first.');
                return;
            }
            document.getElementById('expense-modal').classList.remove('hidden');
        } catch (error) {
            console.error('Error opening expense modal:', error);
            alert('Failed to load categories. Please try again.');
        }
    });

    document.getElementById('add-income-btn').addEventListener('click', async () => {
        try {
            await loadIncomeCategories();
            if (incomeCategories.length === 0) {
                alert('No income categories available. Please create some income categories first.');
                return;
            }
            document.getElementById('income-modal').classList.remove('hidden');
            // Set today's date as default
            document.getElementById('income-date').value = new Date().toISOString().split('T')[0];
        } catch (error) {
            console.error('Error opening income modal:', error);
            alert('Failed to load income categories. Please try again.');
        }
    });

    document.getElementById('add-budget-btn').addEventListener('click', async () => {
        try {
            await loadCategories();
            if (categories.length === 0) {
                alert('No expense categories available. Please create some categories first.');
                return;
            }
            document.getElementById('budget-modal').classList.remove('hidden');
        } catch (error) {
            console.error('Error opening budget modal:', error);
            alert('Failed to load categories. Please try again.');
        }
    });

    document.getElementById('add-income-category-btn').addEventListener('click', () => {
        document.getElementById('income-category-modal').classList.remove('hidden');
    });

    // Admin refresh button
    document.getElementById('refresh-users').addEventListener('click', () => {
        adminManager.refreshAdminData();
    });

    document.getElementById('close-expense-modal').addEventListener('click', () => {
        document.getElementById('expense-modal').classList.add('hidden');
    });

    document.getElementById('close-income-modal').addEventListener('click', () => {
        document.getElementById('income-modal').classList.add('hidden');
    });

    document.getElementById('close-budget-modal').addEventListener('click', () => {
        document.getElementById('budget-modal').classList.add('hidden');
    });

    document.getElementById('close-income-category-modal').addEventListener('click', () => {
        document.getElementById('income-category-modal').classList.add('hidden');
    });

    document.getElementById('close-budget-allocation-modal').addEventListener('click', () => {
        document.getElementById('budget-allocation-modal').classList.add('hidden');
    });

    // Budget allocation form
    document.getElementById('budget-allocation-form').addEventListener('submit', handleBudgetAllocationSubmit);

    // Percentage input listeners for real-time total calculation
    ['essentials-percentage', 'growth-percentage', 'emergency-percentage', 'leisure-percentage'].forEach(id => {
        document.getElementById(id).addEventListener('input', updatePercentageTotal);
    });

    // Tab switching
    document.getElementById('tab-expenses').addEventListener('click', () => switchTab('expenses'));
    document.getElementById('tab-incomes').addEventListener('click', () => switchTab('incomes'));
    document.getElementById('tab-budgets').addEventListener('click', () => switchTab('budgets'));
    document.getElementById('tab-analytics').addEventListener('click', () => switchTab('analytics'));
    document.getElementById('tab-admin').addEventListener('click', () => switchTab('admin'));
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
                lastName: data.lastName,
                role: data.role
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

    // Show/hide admin tab based on user role
    const adminTab = document.getElementById('tab-admin');
    if (adminTab) {
        if (currentUser.role === 'ADMIN') {
            adminTab.style.display = 'block';
        } else {
            adminTab.style.display = 'none';
        }
    }

    await loadInitialData();
}

async function loadInitialData() {
    try {
        // Load budget allocation settings
        loadBudgetAllocation();

        // Load all data in parallel
        await Promise.all([
            loadCategories(),
            loadExpenses(),
            loadBudgets(),
            loadIncomeCategories(),
            loadIncomes()
        ]);

        // Check if we have categories, if not show a helpful message
        if (categories.length === 0) {
            console.warn('No categories found. This might be normal on first run.');
        }

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
            console.log('Categories loaded:', categories); // Debug log
            populateCategorySelects();
        } else {
            console.error('Failed to load categories, status:', response.status);
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

async function loadIncomes() {
    try {
        const response = await apiRequest('/incomes');
        if (response.ok) {
            incomes = await response.json();
            displayIncomes();
        }
    } catch (error) {
        console.error('Error loading incomes:', error);
    }
}

async function loadIncomeCategories() {
    try {
        const response = await apiRequest('/income-categories');
        if (response.ok) {
            incomeCategories = await response.json();
            displayIncomeCategories();
            populateIncomeCategorySelect();
        }
    } catch (error) {
        console.error('Error loading income categories:', error);
    }
}

// UI Update functions
function populateCategorySelects() {
    const expenseSelect = document.getElementById('expense-category');
    const budgetSelect = document.getElementById('budget-category');

    if (!expenseSelect || !budgetSelect) {
        console.error('Category select elements not found');
        return;
    }

    expenseSelect.innerHTML = '<option value="">Select Category</option>';
    budgetSelect.innerHTML = '<option value="">Select Category</option>';

    console.log('Populating categories:', categories); // Debug log

    if (categories && categories.length > 0) {
        categories.forEach(category => {
            const option1 = new Option(category.name, category.id);
            const option2 = new Option(category.name, category.id);
            expenseSelect.appendChild(option1);
            budgetSelect.appendChild(option2);
        });
    } else {
        console.warn('No categories available to populate');
    }
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

function displayIncomes() {
    const container = document.getElementById('incomes-list');

    if (incomes.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-400">
                <i class="fas fa-coins text-4xl mb-4 opacity-50"></i>
                <p>No income recorded yet. Add your first income!</p>
            </div>
        `;
        return;
    }

    container.innerHTML = incomes.slice(0, 10).map(income => {
        const category = incomeCategories.find(c => c.id === income.categoryId);
        const date = new Date(income.date).toLocaleDateString();

        return `
            <div class="transaction-item bg-gray-700 p-4 rounded-lg flex items-center justify-between">
                <div class="flex items-center space-x-4">
                    <div class="w-12 h-12 rounded-lg flex items-center justify-center" style="background-color: ${category?.colorCode || '#10b981'}">
                        <span class="text-xl">${category?.iconName || '💰'}</span>
                    </div>
                    <div>
                        <h4 class="font-medium text-white">${income.description}</h4>
                        <p class="text-sm text-gray-400">${category?.name || 'Unknown'} • ${date}</p>
                    </div>
                </div>
                <div class="text-right">
                    <p class="font-bold text-green-400">+$${income.amount.toFixed ? income.amount.toFixed(2) : income.amount}</p>
                    <button class="text-xs text-gray-500 hover:text-gray-300" onclick="deleteIncome(${income.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function displayIncomeCategories() {
    const container = document.getElementById('income-categories-list');

    if (incomeCategories.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-400">
                <i class="fas fa-tags text-4xl mb-4 opacity-50"></i>
                <p>No income categories yet. Create your first category!</p>
            </div>
        `;
        return;
    }

    container.innerHTML = incomeCategories.map(category => {
        const totalAmount = category.totalAmount || 0;
        const count = category.count || 0;

        return `
            <div class="bg-gray-700 p-4 rounded-lg flex items-center justify-between">
                <div class="flex items-center space-x-4">
                    <div class="w-12 h-12 rounded-lg flex items-center justify-center" style="background-color: ${category.colorCode || '#10b981'}">
                        <span class="text-xl">${category.iconName || '💰'}</span>
                    </div>
                    <div>
                        <h4 class="font-medium text-white">${category.name}</h4>
                        <p class="text-sm text-gray-400">${category.description || ''}</p>
                    </div>
                </div>
                <div class="text-right">
                    <p class="font-bold text-green-400">$${totalAmount.toFixed ? totalAmount.toFixed(2) : totalAmount}</p>
                    <p class="text-xs text-gray-400">${count} income${count !== 1 ? 's' : ''}</p>
                </div>
            </div>
        `;
    }).join('');
}

function populateIncomeCategorySelect() {
    const select = document.getElementById('income-category');

    select.innerHTML = '<option value="">Select Category</option>';

    incomeCategories.forEach(category => {
        const option = new Option(category.name, category.id);
        select.appendChild(option);
    });
}

// Monthly Budget Management Functions
function calculateMonthlyBudget() {
    // Get current month's income
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();

    monthlyBudget.totalIncome = incomes
        .filter(income => {
            const incomeDate = new Date(income.date);
            return incomeDate.getMonth() === currentMonth && incomeDate.getFullYear() === currentYear;
        })
        .reduce((sum, income) => sum + (income.amount || 0), 0);

    // Calculate allocations based on percentages
    monthlyBudget.essentials.allocated = monthlyBudget.totalIncome * (monthlyBudget.essentials.percentage / 100);
    monthlyBudget.growth.allocated = monthlyBudget.totalIncome * (monthlyBudget.growth.percentage / 100);
    monthlyBudget.emergency.allocated = monthlyBudget.totalIncome * (monthlyBudget.emergency.percentage / 100);
    monthlyBudget.leisure.allocated = monthlyBudget.totalIncome * (monthlyBudget.leisure.percentage / 100);

    // Calculate spent amounts by category mapping
    calculateBudgetSpending();
}

function calculateBudgetSpending() {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();

    // Reset spending
    monthlyBudget.essentials.spent = 0;
    monthlyBudget.growth.spent = 0;
    monthlyBudget.emergency.spent = 0;
    monthlyBudget.leisure.spent = 0;

    // Calculate spending for current month
    const currentMonthExpenses = expenses.filter(expense => {
        const expenseDate = new Date(expense.expenseDate);
        return expenseDate.getMonth() === currentMonth && expenseDate.getFullYear() === currentYear;
    });

    currentMonthExpenses.forEach(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        const categoryName = category?.name || 'Other';

        // Determine budget type for this expense
        let budgetType = 'leisure'; // default

        for (const [type, categoryList] of Object.entries(budgetCategoryMapping)) {
            if (categoryList.includes(categoryName)) {
                budgetType = type;
                break;
            }
        }

        monthlyBudget[budgetType].spent += expense.amount;
    });
}

function updateBudgetAllocation(essentials, growth, emergency, leisure) {
    // Validate percentages add up to 100
    const total = essentials + growth + emergency + leisure;
    if (Math.abs(total - 100) > 0.01) {
        alert('Budget percentages must add up to 100%');
        return false;
    }

    monthlyBudget.essentials.percentage = essentials;
    monthlyBudget.growth.percentage = growth;
    monthlyBudget.emergency.percentage = emergency;
    monthlyBudget.leisure.percentage = leisure;

    // Recalculate allocations
    calculateMonthlyBudget();

    // Save to localStorage
    localStorage.setItem('budgetAllocation', JSON.stringify({
        essentials, growth, emergency, leisure
    }));

    return true;
}

function loadBudgetAllocation() {
    const saved = localStorage.getItem('budgetAllocation');
    if (saved) {
        const allocation = JSON.parse(saved);
        monthlyBudget.essentials.percentage = allocation.essentials || 50;
        monthlyBudget.growth.percentage = allocation.growth || 25;
        monthlyBudget.emergency.percentage = allocation.emergency || 15;
        monthlyBudget.leisure.percentage = allocation.leisure || 10;
    }
}

function updateDashboard() {
    // Calculate monthly budget first
    calculateMonthlyBudget();

    // Calculate total balance from income minus expenses
    const totalIncome = incomes.reduce((sum, income) => sum + (income.amount || 0), 0);
    const totalSpent = expenses.reduce((sum, expense) => sum + expense.amount, 0);
    const totalBalance = totalIncome - totalSpent;

    const balanceElement = document.getElementById('total-balance');
    balanceElement.textContent = `$${totalBalance.toFixed(2)}`;
    balanceElement.className = totalBalance >= 0 ? 'text-4xl font-bold mt-2 text-green-400' : 'text-4xl font-bold mt-2 text-red-400';

    // Update monthly budget display
    updateMonthlyBudgetDisplay();

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

// Income functions
async function handleAddIncome(e) {
    e.preventDefault();

    const formData = {
        amount: parseFloat(document.getElementById('income-amount').value),
        description: document.getElementById('income-description').value,
        date: document.getElementById('income-date').value,
        categoryId: parseInt(document.getElementById('income-category').value)
    };

    try {
        const response = await apiRequest('/incomes', {
            method: 'POST',
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            document.getElementById('income-modal').classList.add('hidden');
            document.getElementById('income-form').reset();
            await loadIncomes();
            await loadIncomeCategories(); // Reload to update totals
            updateDashboard();
        } else {
            alert('Failed to add income');
        }
    } catch (error) {
        console.error('Error adding income:', error);
        alert('Failed to add income');
    }
}

async function handleAddIncomeCategory(e) {
    e.preventDefault();

    const formData = {
        name: document.getElementById('income-category-name').value,
        description: document.getElementById('income-category-description').value,
        iconName: document.getElementById('income-category-icon').value || '💰',
        colorCode: document.getElementById('income-category-color').value
    };

    try {
        const response = await apiRequest('/income-categories', {
            method: 'POST',
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            document.getElementById('income-category-modal').classList.add('hidden');
            document.getElementById('income-category-form').reset();
            await loadIncomeCategories();
        } else {
            alert('Failed to create income category');
        }
    } catch (error) {
        console.error('Error creating income category:', error);
        alert('Failed to create income category');
    }
}

async function deleteIncome(incomeId) {
    if (!confirm('Are you sure you want to delete this income?')) return;

    try {
        const response = await apiRequest(`/incomes/${incomeId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            await loadIncomes();
            await loadIncomeCategories();
            updateDashboard();
        } else {
            alert('Failed to delete income');
        }
    } catch (error) {
        console.error('Error deleting income:', error);
        alert('Failed to delete income');
    }
}

function updateMonthlyBudgetDisplay() {
    const container = document.getElementById('monthly-budget-display');
    if (!container) return;

    const currentDate = new Date();
    const monthName = currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

    container.innerHTML = `
        <div class="mb-6">
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-xl font-bold text-white">${monthName} Budget Allocation</h3>
                <button id="edit-budget-allocation" class="text-blue-400 hover:text-blue-300 text-sm">
                    <i class="fas fa-edit mr-1"></i>Edit Allocation
                </button>
            </div>
            <div class="bg-gray-700 p-4 rounded-lg mb-4">
                <div class="flex justify-between items-center">
                    <span class="text-gray-300">Total Monthly Income:</span>
                    <span class="text-green-400 font-bold">$${monthlyBudget.totalIncome.toFixed(2)}</span>
                </div>
            </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            ${createBudgetCard('essentials', '🏠', 'Essentials', 'Rent, utilities, groceries')}
            ${createBudgetCard('growth', '📈', 'Growth', 'Investments, education')}
            ${createBudgetCard('emergency', '🛡️', 'Emergency Fund', 'Savings, emergency reserve')}
            ${createBudgetCard('leisure', '🎉', 'Leisure', 'Entertainment, dining out')}
        </div>
    `;

    // Add event listener for edit button
    document.getElementById('edit-budget-allocation')?.addEventListener('click', showBudgetAllocationModal);
}

function createBudgetCard(type, icon, title, description) {
    const budget = monthlyBudget[type];
    const percentage = ((budget.spent / budget.allocated) * 100) || 0;
    const remaining = budget.allocated - budget.spent;

    let statusColor = 'bg-green-500';
    let textColor = 'text-green-400';

    if (percentage >= 100) {
        statusColor = 'bg-red-500';
        textColor = 'text-red-400';
    } else if (percentage >= 80) {
        statusColor = 'bg-yellow-500';
        textColor = 'text-yellow-400';
    }

    return `
        <div class="bg-gray-700 p-4 rounded-lg">
            <div class="flex items-center mb-3">
                <span class="text-2xl mr-3">${icon}</span>
                <div>
                    <h4 class="font-medium text-white">${title}</h4>
                    <p class="text-xs text-gray-400">${description}</p>
                </div>
            </div>
            
            <div class="mb-3">
                <div class="flex justify-between text-sm mb-1">
                    <span class="text-gray-400">${budget.percentage}% allocated</span>
                    <span class="${textColor}">${percentage.toFixed(1)}% used</span>
                </div>
                <div class="w-full bg-gray-600 rounded-full h-2">
                    <div class="${statusColor} h-2 rounded-full transition-all duration-300" style="width: ${Math.min(percentage, 100)}%"></div>
                </div>
            </div>
            
            <div class="text-sm">
                <div class="flex justify-between mb-1">
                    <span class="text-gray-400">Spent:</span>
                    <span class="text-white">$${budget.spent.toFixed(2)}</span>
                </div>
                <div class="flex justify-between mb-1">
                    <span class="text-gray-400">Budget:</span>
                    <span class="text-white">$${budget.allocated.toFixed(2)}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-gray-400">Remaining:</span>
                    <span class="${remaining >= 0 ? 'text-green-400' : 'text-red-400'}">$${remaining.toFixed(2)}</span>
                </div>
            </div>
        </div>
    `;
}

function showBudgetAllocationModal() {
    document.getElementById('budget-allocation-modal').classList.remove('hidden');

    // Populate current values
    document.getElementById('essentials-percentage').value = monthlyBudget.essentials.percentage;
    document.getElementById('growth-percentage').value = monthlyBudget.growth.percentage;
    document.getElementById('emergency-percentage').value = monthlyBudget.emergency.percentage;
    document.getElementById('leisure-percentage').value = monthlyBudget.leisure.percentage;

    updatePercentageTotal();
}

function updatePercentageTotal() {
    const essentials = parseFloat(document.getElementById('essentials-percentage').value) || 0;
    const growth = parseFloat(document.getElementById('growth-percentage').value) || 0;
    const emergency = parseFloat(document.getElementById('emergency-percentage').value) || 0;
    const leisure = parseFloat(document.getElementById('leisure-percentage').value) || 0;

    const total = essentials + growth + emergency + leisure;
    const totalElement = document.getElementById('percentage-total');

    totalElement.textContent = `${total.toFixed(1)}%`;
    totalElement.className = Math.abs(total - 100) < 0.01 ? 'text-green-400' : 'text-red-400';
}

// Budget Allocation functions
async function handleBudgetAllocationSubmit(e) {
    e.preventDefault();

    const essentials = parseFloat(document.getElementById('essentials-percentage').value);
    const growth = parseFloat(document.getElementById('growth-percentage').value);
    const emergency = parseFloat(document.getElementById('emergency-percentage').value);
    const leisure = parseFloat(document.getElementById('leisure-percentage').value);

    if (updateBudgetAllocation(essentials, growth, emergency, leisure)) {
        document.getElementById('budget-allocation-modal').classList.add('hidden');
        updateDashboard();
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
    } else if (tab === 'incomes') {
        loadIncomes();
        loadIncomeCategories();
    } else if (tab === 'budgets') {
        updateMonthlyBudgetDisplay();
        loadBudgets();
    } else if (tab === 'admin') {
        adminManager.refreshAdminData();
    }
}

// Alias for setActiveTab (used by admin code)
function setActiveTab(tab) {
    switchTab(tab);
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
    const expenseData = {};
    expenses.forEach(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        const categoryName = category?.name || 'Unknown';
        expenseData[categoryName] = (expenseData[categoryName] || 0) + expense.amount;
    });

    // Calculate income by category
    const incomeData = {};
    incomes.forEach(income => {
        const category = incomeCategories.find(c => c.id === income.categoryId);
        const categoryName = category?.name || 'Unknown';
        incomeData[categoryName] = (incomeData[categoryName] || 0) + (income.amount || 0);
    });

    const expenseLabels = Object.keys(expenseData);
    const expenseValues = Object.values(expenseData);
    const incomeLabels = Object.keys(incomeData);
    const incomeValues = Object.values(incomeData);

    // Combine data for comparison
    const allLabels = [...new Set([...expenseLabels, ...incomeLabels])];
    const expenseColors = expenseLabels.map((_, i) => `hsl(${i * 360 / expenseLabels.length}, 70%, 50%)`);

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: expenseLabels,
            datasets: [{
                label: 'Expenses',
                data: expenseValues,
                backgroundColor: expenseColors,
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
                },
                title: {
                    display: true,
                    text: 'Spending by Category',
                    color: 'white'
                }
            }
        }
    });
}

function updateTrendChart() {
    const ctx = document.getElementById('trend-chart');
    if (!ctx) return;

    // Calculate monthly spending and income
    const monthlySpending = {};
    const monthlyIncome = {};

    expenses.forEach(expense => {
        const date = new Date(expense.expenseDate);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        monthlySpending[monthKey] = (monthlySpending[monthKey] || 0) + expense.amount;
    });

    incomes.forEach(income => {
        const date = new Date(income.date);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        monthlyIncome[monthKey] = (monthlyIncome[monthKey] || 0) + (income.amount || 0);
    });

    // Get all unique months and sort them
    const allMonths = [...new Set([...Object.keys(monthlySpending), ...Object.keys(monthlyIncome)])].sort();

    const spendingData = allMonths.map(month => monthlySpending[month] || 0);
    const incomeData = allMonths.map(month => monthlyIncome[month] || 0);

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: allMonths.map(label => {
                const [year, month] = label.split('-');
                return new Date(year, month - 1).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'short'
                });
            }),
            datasets: [{
                label: 'Monthly Income',
                data: incomeData,
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                tension: 0.4,
                fill: false
            }, {
                label: 'Monthly Spending',
                data: spendingData,
                borderColor: '#ef4444',
                backgroundColor: 'rgba(239, 68, 68, 0.1)',
                tension: 0.4,
                fill: false
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        color: 'white',
                        callback: function (value) {
                            return '$' + value.toFixed(0);
                        }
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
                },
                title: {
                    display: true,
                    text: 'Income vs Spending Trend',
                    color: 'white'
                }
            }
        }
    });
}

// Admin functionality
class AdminManager {
    async loadSystemStats() {
        try {
            const response = await fetch('/api/admin/stats');
            if (!response.ok) {
                throw new Error('Failed to load system stats');
            }

            const stats = await response.json();
            this.displaySystemStats(stats);
        } catch (error) {
            console.error('Error loading system stats:', error);
            showError('Failed to load system statistics');
        }
    }

    displaySystemStats(stats) {
        const container = document.getElementById('system-stats');
        container.innerHTML = `
            <div class="stat-card bg-blue-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${stats.totalUsers || 0}</div>
                <div class="text-blue-400 text-sm">Total Users</div>
            </div>
            <div class="stat-card bg-green-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${this.formatCurrency(stats.systemTotalIncome || 0)}</div>
                <div class="text-green-400 text-sm">Total Income</div>
            </div>
            <div class="stat-card bg-red-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${this.formatCurrency(stats.systemTotalExpenses || 0)}</div>
                <div class="text-red-400 text-sm">Total Expenses</div>
            </div>
            <div class="stat-card bg-yellow-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${this.formatCurrency(stats.systemBalance || 0)}</div>
                <div class="text-yellow-400 text-sm">System Balance</div>
            </div>
            <div class="stat-card bg-purple-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${stats.totalExpenses || 0}</div>
                <div class="text-purple-400 text-sm">Total Transactions</div>
            </div>
            <div class="stat-card bg-indigo-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${stats.totalCategories || 0}</div>
                <div class="text-indigo-400 text-sm">Categories</div>
            </div>
            <div class="stat-card bg-pink-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${stats.totalBudgets || 0}</div>
                <div class="text-pink-400 text-sm">Active Budgets</div>
            </div>
            <div class="stat-card bg-teal-600 bg-opacity-20 p-4 rounded-lg">
                <div class="text-2xl font-bold text-white">${stats.totalIncomeCategories || 0}</div>
                <div class="text-teal-400 text-sm">Income Categories</div>
            </div>
        `;
    }

    async loadAllUsers() {
        try {
            const response = await fetch('/api/admin/users');
            if (!response.ok) {
                throw new Error('Failed to load users');
            }

            const users = await response.json();
            this.displayUsers(users);
        } catch (error) {
            console.error('Error loading users:', error);
            showError('Failed to load users');
        }
    }

    displayUsers(users) {
        const tbody = document.getElementById('users-table');
        tbody.innerHTML = users.map(user => `
            <tr class="border-b border-gray-700 hover:bg-gray-800">
                <td class="p-3">
                    <div class="flex items-center">
                        <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-sm mr-3">
                            ${(user.firstName || user.username || 'U').charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <div class="font-medium">${user.firstName || ''} ${user.lastName || ''}</div>
                            <div class="text-gray-400 text-sm">@${user.username}</div>
                        </div>
                    </div>
                </td>
                <td class="p-3 text-gray-300">${user.email}</td>
                <td class="p-3">
                    <span class="text-green-400 font-medium">
                        ${this.formatCurrency(user.totalIncome || 0)}
                    </span>
                    <div class="text-gray-400 text-sm">${user.incomeCount || 0} transactions</div>
                </td>
                <td class="p-3">
                    <span class="text-red-400 font-medium">
                        ${this.formatCurrency(user.totalExpenses || 0)}
                    </span>
                    <div class="text-gray-400 text-sm">${user.expenseCount || 0} transactions</div>
                </td>
                <td class="p-3">
                    <span class="font-medium ${user.balance >= 0 ? 'text-green-400' : 'text-red-400'}">
                        ${this.formatCurrency(user.balance || 0)}
                    </span>
                    <div class="text-gray-400 text-sm">${user.budgetCount || 0} budgets</div>
                </td>
                <td class="p-3">
                    <div class="flex space-x-2">
                        <button onclick="adminManager.viewUserDetails(${user.id})" 
                                class="text-blue-400 hover:text-blue-300 text-sm">
                            <i class="fas fa-eye"></i> View
                        </button>
                        <button onclick="adminManager.deleteUser(${user.id})" 
                                class="text-red-400 hover:text-red-300 text-sm">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    async loadCategoryUsage() {
        try {
            const response = await fetch('/api/admin/categories/usage');
            if (!response.ok) {
                throw new Error('Failed to load category usage');
            }

            const categoryUsage = await response.json();
            this.displayCategoryUsage(categoryUsage);
        } catch (error) {
            console.error('Error loading category usage:', error);
            showError('Failed to load category usage');
        }
    }

    displayCategoryUsage(categoryUsage) {
        const container = document.getElementById('category-usage');
        container.innerHTML = categoryUsage.map(category => `
            <div class="flex items-center justify-between p-4 bg-gray-800 rounded-lg">
                <div class="flex items-center">
                    <div class="w-10 h-10 rounded-lg flex items-center justify-center mr-4" 
                         style="background-color: ${category.colorCode || '#6B7280'}">
                        <i class="fas fa-${category.iconName || 'circle'} text-white"></i>
                    </div>
                    <div>
                        <div class="font-medium text-white">${category.name}</div>
                        <div class="text-gray-400 text-sm">${category.description || 'No description'}</div>
                    </div>
                </div>
                <div class="text-right">
                    <div class="text-white font-medium">${this.formatCurrency(category.totalSpent || 0)}</div>
                    <div class="text-gray-400 text-sm">
                        ${category.expenseCount || 0} expenses • ${category.budgetCount || 0} budgets
                    </div>
                </div>
            </div>
        `).join('');
    }

    async viewUserDetails(userId) {
        try {
            const response = await fetch(`/api/admin/users/${userId}`);
            if (!response.ok) {
                throw new Error('Failed to load user details');
            }

            const userDetails = await response.json();
            this.showUserDetailsModal(userDetails);
        } catch (error) {
            console.error('Error loading user details:', error);
            showError('Failed to load user details');
        }
    }

    showUserDetailsModal(userDetails) {
        // Create modal dynamically
        const modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        modal.innerHTML = `
            <div class="revolut-card p-6 w-full max-w-4xl mx-4 max-h-96 overflow-y-auto">
                <div class="flex justify-between items-center mb-6">
                    <h3 class="text-xl font-bold text-white">User Details: ${userDetails.user.username}</h3>
                    <button onclick="this.parentElement.parentElement.parentElement.remove()" class="text-gray-400 hover:text-white">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <h4 class="text-lg font-medium text-white mb-4">User Information</h4>
                        <div class="space-y-2 text-gray-300">
                            <p><strong>Name:</strong> ${userDetails.user.firstName || ''} ${userDetails.user.lastName || ''}</p>
                            <p><strong>Email:</strong> ${userDetails.user.email}</p>
                            <p><strong>Username:</strong> ${userDetails.user.username}</p>
                            <p><strong>Created:</strong> ${new Date(userDetails.user.createdAt).toLocaleDateString()}</p>
                        </div>
                    </div>
                    <div>
                        <h4 class="text-lg font-medium text-white mb-4">Financial Summary</h4>
                        <div class="space-y-2 text-gray-300">
                            <p><strong>Total Expenses:</strong> ${userDetails.expenses?.length || 0}</p>
                            <p><strong>Total Incomes:</strong> ${userDetails.incomes?.length || 0}</p>
                            <p><strong>Active Budgets:</strong> ${userDetails.budgets?.length || 0}</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }

    async deleteUser(userId) {
        if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
            return;
        }

        try {
            const response = await fetch(`/api/admin/users/${userId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('Failed to delete user');
            }

            showSuccess('User deleted successfully');
            this.loadAllUsers(); // Refresh the list
        } catch (error) {
            console.error('Error deleting user:', error);
            showError('Failed to delete user');
        }
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }

    async refreshAdminData() {
        await Promise.all([
            this.loadSystemStats(),
            this.loadAllUsers(),
            this.loadCategoryUsage()
        ]);
    }
}

// Initialize admin manager
const adminManager = new AdminManager();


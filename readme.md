# Pre-Commit and Commitizen Setup Guide

## 🚀 Introduction
This guide walks you through setting up **pre-commit** and **commitizen** to enforce code quality and commit message conventions in your project.

---

## 📌 Prerequisites
Ensure you have the following installed:
- **Python 3.6+**
- **Git**

---

## 🛠 Step 1: Install Dependencies

Install **pre-commit** and **commitizen** using `pip`:
```sh
pip install pre-commit commitizen
```

Verify installation:
```sh
pre-commit --version
cz --version  # Commitizen
```

---

## 🔧 Step 2: Configure Pre-Commit Hooks

### 2.1 Create `.pre-commit-config.yaml`
In the root of your repository, create a file named **`.pre-commit-config.yaml`** with the following content:

```yaml
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.3.0  # Use the latest stable version
    hooks:
      - id: trailing-whitespace  # Remove trailing whitespace
      - id: end-of-file-fixer  # Ensure the file ends with a newline
      - id: check-yaml  # Validate YAML files
      - id: check-json  # Validate JSON files

  - repo: https://github.com/commitizen-tools/commitizen
    rev: v3.9.0  # Check latest version
    hooks:
      - id: commitizen
        stages: [commit-msg]  # Enforce commit message format
```

### 2.2 Install the Hooks
Run:
```sh
pre-commit install
pre-commit install --hook-type commit-msg  # Install commit message hook
```

Now, the pre-commit hooks will run automatically before each commit.

---

## 📝 Step 3: Enforce Commit Message Format (Custom Hook)

If you require a specific commit message format, such as **"JIRA-123: Fix login issue"**, create a custom commit hook.

### 3.1 Create a Custom Commit Message Hook

Create a new file at `.git/hooks/commit-msg` and add the following content:
```sh
#!/bin/bash
commit_msg_file=$1
commit_msg=$(cat "$commit_msg_file")

# Enforce format: "JIRA-123: Message"
if ! echo "$commit_msg" | grep -qE "^[A-Z]+-[0-9]+: .+"; then
    echo "❌ ERROR: Commit message must follow 'JIRA-123: Message' format."
    exit 1
fi
```

### 3.2 Make the Hook Executable
Run:
```sh
chmod +x .git/hooks/commit-msg
```

### 3.3 Integrate with `pre-commit`
Modify `.pre-commit-config.yaml` to include:

```yaml
  - repo: local
    hooks:
      - id: commit-msg-format
        name: Enforce Commit Message Format
        entry: .git/hooks/commit-msg
        language: system
        stages: [commit-msg]
```

Reinstall the hooks:
```sh
pre-commit install --hook-type commit-msg
```

---

## 🎯 Step 4: Using Commitizen

### 4.1 Configure Commitizen
Run:
```sh
cz init
```
This will generate a `.cz.toml` file with default settings.

### 4.2 Create a Conventional Commit
Instead of using `git commit -m "message"`, run:
```sh
cz commit
```
It will prompt you to select a **commit type**, scope, and description.

### 4.3 Check Commit History
To verify commits follow the convention, run:
```sh
cz check
```
 ### 4.4 Making changes to the hooks
 `pre-commit uninstall && pre-commit install --install-hooks`
---

## ✅ Step 5: Testing the Setup
To ensure everything works:
1. Modify a file and attempt to commit.
2. The pre-commit hooks will check the format and prevent committing if issues are found.
3. Use `cz commit` to follow the commit message convention.

---

## 🎉 Done!
Now your project has automated **code quality checks** and **commit message validation**! 🚀


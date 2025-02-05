@param String error
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - SmartCore</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #0e0e0e;
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            color: #f4f4f4;
            overflow: hidden;
        }

        .login-container {
            display: flex;
            justify-content: center;
            align-items: center;
            background-color: #1e1e1e;
            border-radius: 0.75rem;
            box-shadow: 0px 0.3125rem 1.25rem rgba(0, 0, 0, 0.3);
            width: 80%;
            max-width: 37.5rem;
            padding: 2rem;
            margin: 0;
            transform: scale(1);
            animation: zoomIn 1s ease-out forwards;
        }

        @keyframes zoomIn {
            from {
                transform: scale(1.2);
                opacity: 0;
            }
            to {
                transform: scale(1);
                opacity: 1;
            }
        }

        .login-left {
            flex: 1;
            display: flex;
            justify-content: center;
            align-items: center;
            text-align: center;
            color: #007bff;
        }

        .login-left h1 {
            font-size: 3rem;
            font-family: 'Segoe UI', sans-serif;
            margin: 0;
            color: #fff;
            text-shadow: 0.125rem 0.125rem 0.3125rem rgba(0, 0, 0, 0.4);
        }

        .login-form {
            flex: 1;
            padding: 2rem;
            background-color: #2a2a2a;
            border-radius: 0.625rem;
            box-shadow: 0px 0.3125rem 0.9375rem rgba(0, 0, 0, 0.2);
        }

        .login-form h2 {
            font-size: 1.625rem;
            margin-bottom: 1.25rem;
            color: #fff;
            text-align: center;
            font-family: 'Segoe UI', sans-serif;
        }

        .login-form input {
            width: 100%;
            padding: 0.625rem;
            margin: 0.625rem 0;
            border: none;
            border-radius: 0.375rem;
            background-color: #333;
            color: #f4f4f4;
            font-size: 1rem;
            transition: background-color 0.3s ease;
        }

        .login-form input:focus {
            background-color: #555;
            outline: none;
        }

        .login-form button {
            width: 100%;
            padding: 0.75rem;
            background-color: #007bff;
            color: #fff;
            border: none;
            border-radius: 0.375rem;
            font-size: 1.125rem;
            cursor: pointer;
            transition: background-color 0.3s ease;
        }

        .login-form button:hover {
            background-color: #0056b3;
        }

        .error {
            color: #ff4c4c;
            text-align: center;
            margin-bottom: 1.25rem;
            font-size: 0.750rem;
        }

        .error-message {
            margin: 0.625rem 0;
            padding: 0.625rem;
            background-color: #e14e4e;
            border-radius: 0.375rem;
        }

        .login-left h1 {
            animation: fadeIn 1.5s ease-out;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
            }
            to {
                opacity: 1;
            }
        }

        @media (max-width: 768px) {
            .login-container {
                width: 90%;
            }

            .login-left h1 {
                font-size: 2rem;
            }

            .login-form h2 {
                font-size: 1.25rem;
            }

            .login-form input {
                font-size: 0.875rem;
            }

            .login-form button {
                font-size: 1rem;
            }
        }

    </style>
</head>
<body>
<div class="login-container">
    <div class="login-left">
        <h1>SmartCore</h1>
    </div>
    <div class="login-form">
        <h2>Login</h2>

        <!-- Error message (JTE template error handling) -->
        @if (error != null)
            <div class="error">
                <div class="error-message">${error}</div>
            </div>
        @endif

        <!-- AJAX Login Form -->
        <form id="loginForm">
            <label for="username"></label><input type="text" name="username" id="username" placeholder="Username" required>
            <label for="password"></label><input type="password" name="password" id="password" placeholder="Password" required>
            <button type="submit">Login</button>
        </form>

        <!-- Error message (AJAX handling) -->
        <div id="errorMessage" class="error" style="display: none;"></div>

    </div>
</div>

<script>
    document.getElementById("loginForm").addEventListener("submit", function (event) {
        event.preventDefault(); // Предотвращаем стандартную отправку формы

        let formData = new FormData(this);

        fetch("/api/auth/login", {
            method: "POST",
            body: new URLSearchParams(formData),
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    document.getElementById("errorMessage").innerText = data.error;
                    document.getElementById("errorMessage").style.display = "block";
                } else {
                    window.location.href = "/dashboard"; // Перенаправление на нужную страницу после успешного входа
                }
            })
            .catch(error => console.error("Login error:", error));
    });
</script>

</body>
</html>

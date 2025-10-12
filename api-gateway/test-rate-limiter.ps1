# PowerShell script to test rate limiter
$url = "http://localhost:8888/api/v1/identity/auth/login"
$headers = @{
    "Content-Type" = "application/json"
}
$body = @{
    username = "test"
    password = "test"
} | ConvertTo-Json

Write-Host "Testing Rate Limiter - Sending 15 requests rapidly..." -ForegroundColor Yellow
Write-Host "Expected: First 10 requests should pass, then 429 errors should start appearing" -ForegroundColor Cyan

for ($i = 1; $i -le 15; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $url -Method POST -Headers $headers -Body $body -ErrorAction Stop
        Write-Host "Request $i`: SUCCESS (Status: 200)" -ForegroundColor Green
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 429) {
            Write-Host "Request $i`: RATE LIMITED (Status: 429) ✅" -ForegroundColor Red
        } else {
            Write-Host "Request $i`: Other Error (Status: $statusCode)" -ForegroundColor Yellow
        }
    }
    Start-Sleep -Milliseconds 100  # Small delay to ensure rapid succession
}

Write-Host "`nWaiting 3 seconds for token replenishment..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

Write-Host "Sending 3 more requests after waiting..." -ForegroundColor Yellow
for ($i = 16; $i -le 18; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $url -Method POST -Headers $headers -Body $body -ErrorAction Stop
        Write-Host "Request $i`: SUCCESS after waiting (Status: 200)" -ForegroundColor Green
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Request $i`: Error after waiting (Status: $statusCode)" -ForegroundColor Yellow
    }
}
